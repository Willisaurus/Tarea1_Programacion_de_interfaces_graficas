package vista;

import java.awt.Color;

/**
 * Centralises all color definitions for the application's visual themes.
 *
 * DARK MODE  – Catppuccin Mocha palette
 *   A rich dark-purple palette with pastel accent colours.
 *   References: https://github.com/catppuccin/catppuccin
 *
 * LIGHT MODE – Soft-Indigo palette
 *   Clean white/slate backgrounds with an indigo/violet accent family.
 */
public class ThemeManager {

    // ─── Theme enum ──────────────────────────────────────────────────────────
    public enum Theme { LIGHT, DARK }

    private static Theme currentTheme = Theme.LIGHT;

    // ─── Dark palette  (Catppuccin Mocha) ────────────────────────────────────
    // Background layers
    private static final Color D_CRUST   = new Color(0x11, 0x11, 0x1B); // #11111B
    private static final Color D_MANTLE  = new Color(0x18, 0x18, 0x25); // #181825
    private static final Color D_BASE    = new Color(0x1E, 0x1E, 0x2E); // #1E1E2E
    private static final Color D_SURF0   = new Color(0x31, 0x32, 0x44); // #313244
    private static final Color D_SURF1   = new Color(0x45, 0x47, 0x5A); // #45475A
    private static final Color D_OVERLAY = new Color(0x6C, 0x70, 0x86); // #6C7086
    // Text
    private static final Color D_TEXT    = new Color(0xCD, 0xD6, 0xF4); // #CDD6F4
    private static final Color D_SUBTEXT = new Color(0xBA, 0xC2, 0xDE); // #BAC2DE
    // Accent family
    private static final Color D_MAUVE   = new Color(0xCB, 0xA6, 0xF7); // #CBA6F7  ← main accent
    private static final Color D_LAVEND  = new Color(0xB4, 0xBE, 0xFE); // #B4BEFE
    private static final Color D_BLUE    = new Color(0x89, 0xB4, 0xFA); // #89B4FA
    private static final Color D_SAPPH   = new Color(0x74, 0xC7, 0xEC); // #74C7EC
    private static final Color D_SKY     = new Color(0x89, 0xDC, 0xEB); // #89DCEB
    private static final Color D_TEAL    = new Color(0x94, 0xE2, 0xD5); // #94E2D5
    private static final Color D_GREEN   = new Color(0xA6, 0xE3, 0xA1); // #A6E3A1
    private static final Color D_YELLOW  = new Color(0xF9, 0xE2, 0xAF); // #F9E2AF
    private static final Color D_PEACH   = new Color(0xFA, 0xB3, 0x87); // #FAB387
    private static final Color D_RED     = new Color(0xF3, 0x8B, 0xA8); // #F38BA8

    // ─── Light palette  (Soft-Indigo) ─────────────────────────────────────────
    private static final Color L_BG      = new Color(0xF1, 0xF5, 0xF9); // #F1F5F9
    private static final Color L_PANEL   = new Color(0xFF, 0xFF, 0xFF); // #FFFFFF
    private static final Color L_SURFACE = new Color(0xF8, 0xFA, 0xFC); // #F8FAFC
    private static final Color L_BORDER  = new Color(0xE2, 0xE8, 0xF0); // #E2E8F0
    private static final Color L_TEXT    = new Color(0x1E, 0x29, 0x3B); // #1E293B
    private static final Color L_SUBTEXT = new Color(0x64, 0x74, 0x8B); // #64748B
    private static final Color L_INDIGO  = new Color(0x63, 0x66, 0xF1); // #6366F1  ← main accent
    private static final Color L_VIOLET  = new Color(0x7C, 0x3A, 0xED); // #7C3AED
    private static final Color L_BLUE    = new Color(0x25, 0x63, 0xEB); // #2563EB
    private static final Color L_TEAL    = new Color(0x0D, 0x94, 0x88); // #0D9488
    private static final Color L_GREEN   = new Color(0x16, 0xA3, 0x4A); // #16A34A
    private static final Color L_ORANGE  = new Color(0xEA, 0x58, 0x0C); // #EA580C
    private static final Color L_RED     = new Color(0xDC, 0x26, 0x26); // #DC2626

    // ─── Public API ─────────────────────────────────────────────────────────
    public static Theme  getCurrentTheme()       { return currentTheme; }
    public static void   setTheme(Theme t)       { currentTheme = t; }
    public static void   toggleTheme()           { currentTheme = isDark() ? Theme.LIGHT : Theme.DARK; }
    public static boolean isDark()               { return currentTheme == Theme.DARK; }

    // ─── Background / surface colours ────────────────────────────────────────
    /** Main window background */
    public static Color getBg()            { return isDark() ? D_BASE   : L_BG; }
    /** Panel / card background */
    public static Color getPanelBg()       { return isDark() ? D_MANTLE : L_PANEL; }
    /** Input field background */
    public static Color getFieldBg()       { return isDark() ? D_SURF0  : L_SURFACE; }
    /** Border / separator colour */
    public static Color getBorderColor()   { return isDark() ? D_SURF1  : L_BORDER; }
    /** Muted overlay (disabled, placeholders) */
    public static Color getOverlay()       { return isDark() ? D_OVERLAY : L_SUBTEXT; }

    // ─── Text colours ─────────────────────────────────────────────────────────
    public static Color getText()          { return isDark() ? D_TEXT    : L_TEXT; }
    public static Color getTextSecondary() { return isDark() ? D_SUBTEXT : L_SUBTEXT; }

    // ─── Accent colours ───────────────────────────────────────────────────────
    /** Primary accent (used for title, progress bar, theme button) */
    public static Color getAccent()        { return isDark() ? D_MAUVE   : L_INDIGO; }
    public static Color getAccent2()       { return isDark() ? D_LAVEND  : L_VIOLET; }

    // ─── Table colours ────────────────────────────────────────────────────────
    public static Color getTableHeaderBg()    { return isDark() ? D_SURF0  : L_INDIGO; }
    public static Color getTableHeaderFg()    { return Color.WHITE; }
    public static Color getTableRowBg()       { return isDark() ? D_BASE   : L_PANEL; }
    public static Color getTableAltRowBg()    { return isDark() ? D_MANTLE : L_SURFACE; }
    public static Color getTableSelectionBg() { return isDark() ? D_MAUVE  : L_INDIGO; }
    public static Color getTableGridColor()   { return isDark() ? D_SURF1  : L_BORDER; }

    // ─── Action-button colours ────────────────────────────────────────────────
    public static Color getBtnAdd()    { return isDark() ? D_GREEN  : L_GREEN; }
    public static Color getBtnModify() { return isDark() ? D_BLUE   : L_BLUE; }
    public static Color getBtnDelete() { return isDark() ? D_RED    : L_RED; }
    public static Color getBtnExport() { return isDark() ? D_TEAL   : L_TEAL; }
    public static Color getBtnImport() { return isDark() ? D_PEACH  : L_ORANGE; }
    public static Color getBtnTheme()  { return isDark() ? D_MAUVE  : L_VIOLET; }
}
