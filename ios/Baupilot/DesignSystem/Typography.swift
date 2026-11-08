import SwiftUI

public enum InterWeight: Int {
    case regular = 400, medium = 500, semibold = 600, bold = 700

    var postScriptName: String {
        switch self {
        case .regular: return "Inter-Regular"
        case .medium: return "Inter-Medium"
        case .semibold: return "Inter-SemiBold"
        case .bold: return "Inter-Bold"
        }
    }
}

public extension Font {
    /// Inter at a fixed point size. Letter spacing is applied per call site with `.tracking(_:)`.
    static func inter(_ size: CGFloat, _ weight: InterWeight) -> Font {
        Font.custom(weight.postScriptName, size: size)
    }
}
