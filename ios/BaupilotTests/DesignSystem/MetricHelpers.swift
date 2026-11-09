import SwiftUI
import UIKit

/// Natural size of a component laid out at a fixed width. For components only, never `ScreenScaffold`;
/// greedy views report the proposal.
@MainActor
func size(of view: some View, width: CGFloat) -> CGSize {
    let hosting = UIHostingController(
        rootView: view.frame(width: width).fixedSize(horizontal: false, vertical: true))
    return hosting.sizeThatFits(in: CGSize(width: width, height: .greatestFiniteMagnitude))
}

/// RGBA pixels of a view rendered at a fixed size and scale (premultiplied, row 0 on top).
struct Pixels {
    let width: Int
    let height: Int
    let rgba: [UInt8]

    func pixel(_ x: Int, _ y: Int) -> [UInt8] {
        let offset = (y * width + x) * 4
        return Array(rgba[offset..<offset + 4])
    }
}

/// Renders `view` into a `size` frame at `scale`; the displayScale environment matches so scale-aware views agree.
@MainActor
func render(_ view: some View, size: CGSize, scale: CGFloat) -> Pixels {
    let renderer = ImageRenderer(
        content: view.environment(\.displayScale, scale).frame(width: size.width, height: size.height))
    renderer.scale = scale
    guard let image = renderer.cgImage else { fatalError("ImageRenderer produced no image") }
    let width = image.width
    let height = image.height
    var data = [UInt8](repeating: 0, count: width * height * 4)
    let drawn = data.withUnsafeMutableBytes { buffer -> Bool in
        guard
            let context = CGContext(
                data: buffer.baseAddress, width: width, height: height, bitsPerComponent: 8, bytesPerRow: width * 4,
                space: CGColorSpaceCreateDeviceRGB(),
                bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue)
        else { return false }
        context.draw(image, in: CGRect(x: 0, y: 0, width: width, height: height))
        return true
    }
    precondition(drawn, "bitmap context")
    return Pixels(width: width, height: height, rgba: data)
}
