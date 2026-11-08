import CoreText
import Foundation

enum BaupilotFonts {
    static let postScriptNames = ["Inter-Regular", "Inter-Medium", "Inter-SemiBold", "Inter-Bold"]

    /// Registers the bundled Inter TTFs; safe to call more than once.
    static func registerIfNeeded() {
        for name in postScriptNames {
            guard let url = Bundle.main.url(forResource: name, withExtension: "ttf") else {
                assertionFailure("Font file \(name).ttf missing from bundle")
                continue
            }
            var error: Unmanaged<CFError>?
            if !CTFontManagerRegisterFontsForURL(url as CFURL, .process, &error) {
                let cfError = error?.takeRetainedValue()
                let code = cfError.map { CFErrorGetCode($0) }
                if code != CFIndex(CTFontManagerError.alreadyRegistered.rawValue) {
                    assertionFailure("Font registration failed for \(name): \(String(describing: cfError))")
                }
            }
        }
    }
}
