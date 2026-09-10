import AVFoundation
import MediaPlayer

/// Plays the full-length azan — the part a notification cannot do.
///
/// A notification sound is capped at 30 seconds, so the complete azan is only heard
/// when the prayer arrives while the app is open. That is the one moment iOS hands the
/// app control, and it is worth taking.
///
/// The app declares the `audio` background mode so that locking the phone part-way
/// through does not cut the call off. That comes with an obligation: an app playing in
/// the background must be visible and controllable from the lock screen, which is what
/// the now-playing info and the remote commands below are for.
@MainActor
final class AzanPlayer: NSObject, ObservableObject {

    @Published private(set) var playing: PrayerName?

    private var player: AVAudioPlayer?
    private var remoteCommandsWired = false

    func play(for prayer: PrayerName) {
        stop()

        let name = prayer == .fajr ? "azan_fajr" : "azan"
        guard let url = Bundle.main.url(forResource: name, withExtension: "mp3") else { return }

        // `.playback` is what makes the azan audible with the ring switch flipped to
        // silent — someone who silenced their phone still expects the azan from a
        // prayer app installed for exactly this — and what allows it to continue in the
        // background.
        try? AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)
        try? AVAudioSession.sharedInstance().setActive(true)

        player = try? AVAudioPlayer(contentsOf: url)
        player?.delegate = self
        player?.prepareToPlay()

        guard player?.play() == true else {
            releaseSession()
            return
        }

        playing = prayer
        wireRemoteCommands()
        publishNowPlaying(for: prayer)
        observeInterruptions()
    }

    func stop() {
        player?.stop()
        player = nil
        playing = nil
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nil
        releaseSession()
    }

    private func releaseSession() {
        // Deactivated as soon as playback ends so the app does not sit on the audio
        // route or keep other apps ducked.
        try? AVAudioSession.sharedInstance().setActive(false, options: .notifyOthersOnDeactivation)
    }

    // MARK: - Lock screen

    /// What the lock screen and Control Centre show while the azan plays.
    private func publishNowPlaying(for prayer: PrayerName) {
        var info: [String: Any] = [
            MPMediaItemPropertyTitle: "اذان \(prayer.dari)",
            MPMediaItemPropertyArtist: "اوقات نماز"
        ]
        if let player {
            info[MPMediaItemPropertyPlaybackDuration] = player.duration
            info[MPNowPlayingInfoPropertyElapsedPlaybackTime] = player.currentTime
            info[MPNowPlayingInfoPropertyPlaybackRate] = 1.0
        }
        MPNowPlayingInfoCenter.default().nowPlayingInfo = info
    }

    /// Stop is the only control that makes sense for an azan: it is one recording,
    /// played once, and there is nothing to skip to. Pause is mapped to stop for the
    /// same reason — resuming an azan half an hour later would be worse than silence.
    private func wireRemoteCommands() {
        guard !remoteCommandsWired else { return }
        remoteCommandsWired = true

        let center = MPRemoteCommandCenter.shared()
        center.playCommand.isEnabled = false
        center.nextTrackCommand.isEnabled = false
        center.previousTrackCommand.isEnabled = false

        center.pauseCommand.addTarget { [weak self] _ in
            Task { @MainActor in self?.stop() }
            return .success
        }
        center.stopCommand.addTarget { [weak self] _ in
            Task { @MainActor in self?.stop() }
            return .success
        }
    }

    // MARK: - Interruptions

    /// A phone call, or another app taking the audio route, ends the azan rather than
    /// leaving a stalled player holding the session.
    private func observeInterruptions() {
        NotificationCenter.default.removeObserver(
            self,
            name: AVAudioSession.interruptionNotification,
            object: nil
        )
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleInterruption(_:)),
            name: AVAudioSession.interruptionNotification,
            object: nil
        )
    }

    /// `nonisolated` because the notification is delivered on whichever thread the
    /// audio system happens to be on, not the main one.
    @objc nonisolated private func handleInterruption(_ note: Notification) {
        guard
            let raw = note.userInfo?[AVAudioSessionInterruptionTypeKey] as? UInt,
            let type = AVAudioSession.InterruptionType(rawValue: raw),
            type == .began
        else { return }

        Task { @MainActor in self.stop() }
    }
}

extension AzanPlayer: AVAudioPlayerDelegate {
    nonisolated func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        Task { @MainActor in
            self.stop()
        }
    }
}
