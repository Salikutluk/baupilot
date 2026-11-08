import SwiftUI

/// Login (Main.dc.html): own layout, no `ScreenScaffold`. The bottom block is pinned to the bottom.
struct LoginScreen: View {
    let model: LoginModel
    let onSubmit: () -> Void

    @State private var email = ""
    @State private var password = ""

    private var brand: some View {
        HStack(spacing: 10) {
            IconTile(
                icon: ScreenCopy.Login.brandIcon, size: .brand40, background: Tokens.accent,
                foreground: Tokens.onAccent)
            Text(model.title)
                .interText(20, .bold, tracking: -0.3)
                .foregroundStyle(Tokens.text.color)
        }
    }

    private static let introLineBox: CGFloat = 15 * 1.45
    /// Chrome puts the paragraph's lines a whole point apart (21.75 rounds to 22), and the
    /// intro wraps to three lines.
    private static let introLineStep = introLineBox.rounded()
    private static let introLines: CGFloat = 3
    /// Half-leading above the first line.
    private var introHalfLeading: CGFloat { (Self.introLineBox - InterMetrics.lineBox(15)) / 2 }
    /// `lineSpacing` between lines: the step minus the natural line height.
    private var introLineSpacing: CGFloat { Self.introLineStep - InterMetrics.naturalHeight(15) }
    /// The block is as tall as three CSS line boxes, rounded: the reference lays the features out as if it were
    /// 65 pt (3 x 21.75 = 65.25; measured: the first feature tile sits 3 pt below a 292 pt block end).
    private var introHeight: CGFloat { (Self.introLines * Self.introLineBox).rounded() }

    private var intro: some View {
        VStack(alignment: .leading, spacing: 10) {
            // The two headline lines are separate 34.5 pt line boxes (30 x 1.15, the `<br>` split).
            VStack(alignment: .leading, spacing: 0) {
                ForEach(Array(model.headline.enumerated()), id: \.offset) { index, line in
                    Text(line)
                        .interText(30, .bold, tracking: -0.6, lineHeight: .factor(1.15))
                        .foregroundStyle(Tokens.text.color)
                }
            }
            Text(model.intro)
                .font(.inter(15, .regular))
                .lineSpacing(introLineSpacing)
                .foregroundStyle(Tokens.muted.color)
                .padding(.top, introHalfLeading)
                .frame(maxWidth: .infinity, alignment: .topLeading)
                .frame(height: introHeight, alignment: .top)
        }
    }

    private var features: some View {
        VStack(alignment: .leading, spacing: 10) {
            ForEach(Array(model.features.enumerated()), id: \.offset) { index, feature in
                FeatureRow(icon: ScreenCopy.Login.featureIcons[index], text: feature)
            }
        }
    }

    private var form: some View {
        VStack(alignment: .leading, spacing: 12) {
            FormField(
                label: model.emailLabel, text: $email, placeholder: model.emailPlaceholder,
                kind: .email, border: Tokens.border.login)
            FormField(
                label: model.passwordLabel, text: $password,
                placeholder: ScreenCopy.passwordPlaceholder, kind: .password,
                border: Tokens.border.login,
                trailingLink: FormField.Link(title: model.forgot, action: nil))
        }
        .padding(.top, 4)
    }

    private var newHere: some View {
        HStack(spacing: 0) {
            Text(model.newHere + ScreenCopy.Login.newHereSeparator)
                .interText(14, .regular)
                .foregroundStyle(Tokens.muted.color)
            Button(action: {}) {
                Text(model.create)
                    .interText(14, .semibold)
                    .foregroundStyle(Tokens.accent.color)
                    .frame(minHeight: 44)
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .padding(.vertical, -14)
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 6)
    }

    private var actions: some View {
        VStack(spacing: 10) {
            PrimaryButton(model.submit, height: .tall, action: onSubmit)
                .accessibilityIdentifier("login.submit")
            InvertedButton(model.social, icon: .apple, action: nil)
            newHere
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 22) {
            brand
            intro
            features
            form
            Spacer(minLength: 0)
            actions
        }
        .padding(.top, 64)
        .padding(.horizontal, 24)
        .padding(.bottom, 32)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .background(Tokens.bg.color)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.login")
    }
}
