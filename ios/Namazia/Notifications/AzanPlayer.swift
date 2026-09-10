import AVFoundation

/// Plays the full-length azan — the part a notification cannot do.
///
/// A notification sound is capped at 30 seconds, so the complete azan is only heard
/// when the prayer arrives while the app is open. That is the one moment iOS hands the
/// app control, and it is worth taking.
@MainActor
final class AzanPlayer: NSObject, ObservableObject {

    @Published private(set) var playing: PrayerName?

    private var player: AVAudioPlayer?

    func play(for prayer: PrayerName) {
        stop()

        let name = prayer == .fajr ? "azan_fajr" : "azan"
        guard let url = Bundle.main.url(forResource: name, withExtension: "mp3") else { return }

        // `.playback` is what makes the azan audible with the ring switch flipped to
        // silent. That is deliberate — someone who silenced their phone still expects
        // the azan from a prayer app they installed for exactly this — but it is also
        // why the session is deactivated the moment playback ends, so the app does not
        // sit on the audio route or keep other apps ducked.
        try? AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)
        try? AVAudioSession.sharedInstance().setActive(true)

        player = try? AVAudioPlayer(contentsOf: url)
        player?.delegate = self
        player?.prepareToPlay()

        if player?.play() == true {
            playing = prayer
        } else {
            releaseSession()
        }
    }

    func stop() {
        player?.stop()
        player = nil
        playing = nil
        releaseSession()
    }

    private func releaseSession() {
        try? AVAudioSession.sharedInstance().setActive(false, options: .notifyOthersOnDeactivation)
    }
}

extension AzanPlayer: AVAudioPlayerDelegate {
    nonisolated func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        Task { @MainActor in
            self.player = nil
            self.playing = nil
            self.releaseSession()
        }
    }
}
