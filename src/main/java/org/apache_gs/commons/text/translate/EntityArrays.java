/*
 * Copyright 2022 The graph-support project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache_gs.commons.text.translate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Class holding various entity data for HTML and XML - generally for use with
 * the LookupTranslator.
 * All Maps are generated using {@code java.util.Collections.unmodifiableMap()}.
 *
 * @since 1.0
 */
public class EntityArrays {

   /**
     * A Map&lt;CharSequence, CharSequence&gt; to escape
     * <a href="https://secure.wikimedia.org/wikipedia/en/wiki/ISO/IEC_8859-1">ISO-8859-1</a>
     * characters to their named HTML 3.x equivalents.
     */
    public static final Map<CharSequence, CharSequence> ISO8859_1_ESCAPE;
    static {
        final Map<CharSequence, CharSequence> initialMap = new HashMap<>();
        initialMap.put("\u00A0", "&nbsp;"); // non-breaking space
        initialMap.put("¡", "&iexcl;"); // inverted exclamation mark
        initialMap.put("¢", "&cent;"); // cent sign
        initialMap.put("£", "&pound;"); // pound sign
        initialMap.put("¤", "&curren;"); // currency sign
        initialMap.put("¥", "&yen;"); // yen sign = yuan sign
        initialMap.put("¦", "&brvbar;"); // broken bar = broken vertical bar
        initialMap.put("§", "&sect;"); // section sign
        initialMap.put("¨", "&uml;"); // diaeresis = spacing diaeresis
        initialMap.put("©", "&copy;"); // © - copyright sign
        initialMap.put("ª", "&ordf;"); // feminine ordinal indicator
        initialMap.put("«", "&laquo;"); // left-pointing double angle quotation mark = left pointing guillemet
        initialMap.put("¬", "&not;"); // not sign
        initialMap.put("\u00AD", "&shy;"); // soft hyphen = discretionary hyphen
        initialMap.put("®", "&reg;"); // ® - registered trademark sign
        initialMap.put("¯", "&macr;"); // macron = spacing macron = overline = APL overbar
        initialMap.put("°", "&deg;"); // degree sign
        initialMap.put("±", "&plusmn;"); // plus-minus sign = plus-or-minus sign
        initialMap.put("²", "&sup2;"); // superscript two = superscript digit two = squared
        initialMap.put("³", "&sup3;"); // superscript three = superscript digit three = cubed
        initialMap.put("´", "&acute;"); // acute accent = spacing acute
        initialMap.put("µ", "&micro;"); // micro sign
        initialMap.put("¶", "&para;"); // pilcrow sign = paragraph sign
        initialMap.put("·", "&middot;"); // middle dot = Georgian comma = Greek middle dot
        initialMap.put("¸", "&cedil;"); // cedilla = spacing cedilla
        initialMap.put("¹", "&sup1;"); // superscript one = superscript digit one
        initialMap.put("º", "&ordm;"); // masculine ordinal indicator
        initialMap.put("»", "&raquo;"); // right-pointing double angle quotation mark = right pointing guillemet
        initialMap.put("¼", "&frac14;"); // vulgar fraction one quarter = fraction one quarter
        initialMap.put("½", "&frac12;"); // vulgar fraction one half = fraction one half
        initialMap.put("¾", "&frac34;"); // vulgar fraction three quarters = fraction three quarters
        initialMap.put("¿", "&iquest;"); // inverted question mark = turned question mark
        initialMap.put("À", "&Agrave;"); // À - uppercase A, grave accent
        initialMap.put("Á", "&Aacute;"); // Á - uppercase A, acute accent
        initialMap.put("Â", "&Acirc;"); // Â - uppercase A, circumflex accent
        initialMap.put("Ã", "&Atilde;"); // Ã - uppercase A, tilde
        initialMap.put("Ä", "&Auml;"); // Ä - uppercase A, umlaut
        initialMap.put("Å", "&Aring;"); // � - uppercase A, ring
        initialMap.put("Æ", "&AElig;"); // Æ - uppercase AE
        initialMap.put("Ç", "&Ccedil;"); // Ç - uppercase C, cedilla
        initialMap.put("È", "&Egrave;"); // È - uppercase E, grave accent
        initialMap.put("É", "&Eacute;"); // É - uppercase E, acute accent
        initialMap.put("Ê", "&Ecirc;"); // Ê - uppercase E, circumflex accent
        initialMap.put("Ë", "&Euml;"); // Ë - uppercase E, umlaut
        initialMap.put("Ì", "&Igrave;"); // Ì - uppercase I, grave accent
        initialMap.put("Í", "&Iacute;"); // Í - uppercase I, acute accent
        initialMap.put("Î", "&Icirc;"); // Î - uppercase I, circumflex accent
        initialMap.put("Ï", "&Iuml;"); // Ï - uppercase I, umlaut
        initialMap.put("Ð", "&ETH;"); // Ð - uppercase Eth, Icelandic
        initialMap.put("Ñ", "&Ntilde;"); // Ñ - uppercase N, tilde
        initialMap.put("Ò", "&Ograve;"); // Ò - uppercase O, grave accent
        initialMap.put("Ó", "&Oacute;"); // Ó - uppercase O, acute accent
        initialMap.put("Ô", "&Ocirc;"); // Ô - uppercase O, circumflex accent
        initialMap.put("Õ", "&Otilde;"); // Õ - uppercase O, tilde
        initialMap.put("Ö", "&Ouml;"); // Ö - uppercase O, umlaut
        initialMap.put("×", "&times;"); // multiplication sign
        initialMap.put("Ø", "&Oslash;"); // Ø - uppercase O, slash
        initialMap.put("Ù", "&Ugrave;"); // Ù - uppercase U, grave accent
        initialMap.put("Ú", "&Uacute;"); // Ú - uppercase U, acute accent
        initialMap.put("Û", "&Ucirc;"); // Û - uppercase U, circumflex accent
        initialMap.put("Ü", "&Uuml;"); // Ü - uppercase U, umlaut
        initialMap.put("Ý", "&Yacute;"); // Ý - uppercase Y, acute accent
        initialMap.put("Þ", "&THORN;"); // Þ - uppercase THORN, Icelandic
        initialMap.put("ß", "&szlig;"); // ß - lowercase sharps, German
        initialMap.put("à", "&agrave;"); // à - lowercase a, grave accent
        initialMap.put("á", "&aacute;"); // á - lowercase a, acute accent
        initialMap.put("â", "&acirc;"); // â - lowercase a, circumflex accent
        initialMap.put("ã", "&atilde;"); // ã - lowercase a, tilde
        initialMap.put("ä", "&auml;"); // ä - lowercase a, umlaut
        initialMap.put("å", "&aring;"); // å - lowercase a, ring
        initialMap.put("æ", "&aelig;"); // æ - lowercase ae
        initialMap.put("ç", "&ccedil;"); // ç - lowercase c, cedilla
        initialMap.put("è", "&egrave;"); // è - lowercase e, grave accent
        initialMap.put("é", "&eacute;"); // é - lowercase e, acute accent
        initialMap.put("ê", "&ecirc;"); // ê - lowercase e, circumflex accent
        initialMap.put("ë", "&euml;"); // ë - lowercase e, umlaut
        initialMap.put("ì", "&igrave;"); // ì - lowercase i, grave accent
        initialMap.put("í", "&iacute;"); // í - lowercase i, acute accent
        initialMap.put("î", "&icirc;"); // î - lowercase i, circumflex accent
        initialMap.put("ï", "&iuml;"); // ï - lowercase i, umlaut
        initialMap.put("ð", "&eth;"); // ð - lowercase eth, Icelandic
        initialMap.put("ñ", "&ntilde;"); // ñ - lowercase n, tilde
        initialMap.put("ò", "&ograve;"); // ò - lowercase o, grave accent
        initialMap.put("ó", "&oacute;"); // ó - lowercase o, acute accent
        initialMap.put("ô", "&ocirc;"); // ô - lowercase o, circumflex accent
        initialMap.put("õ", "&otilde;"); // õ - lowercase o, tilde
        initialMap.put("ö", "&ouml;"); // ö - lowercase o, umlaut
        initialMap.put("÷", "&divide;"); // division sign
        initialMap.put("ø", "&oslash;"); // ø - lowercase o, slash
        initialMap.put("ù", "&ugrave;"); // ù - lowercase u, grave accent
        initialMap.put("ú", "&uacute;"); // ú - lowercase u, acute accent
        initialMap.put("û", "&ucirc;"); // û - lowercase u, circumflex accent
        initialMap.put("ü", "&uuml;"); // ü - lowercase u, umlaut
        initialMap.put("ý", "&yacute;"); // ý - lowercase y, acute accent
        initialMap.put("þ", "&thorn;"); // þ - lowercase thorn, Icelandic
        initialMap.put("ÿ", "&yuml;"); // ÿ - lowercase y, umlaut
        ISO8859_1_ESCAPE = Collections.unmodifiableMap(initialMap);
    }

    /**
     * Reverse of {@link #ISO8859_1_ESCAPE} for unescaping purposes.
     */
    public static final Map<CharSequence, CharSequence> ISO8859_1_UNESCAPE;

    static {
        ISO8859_1_UNESCAPE = Collections.unmodifiableMap(invert(ISO8859_1_ESCAPE));
    }

    /**
     * A Map&lt;CharSequence, CharSequence&gt; to escape additional
     * <a href="http://www.w3.org/TR/REC-html40/sgml/entities.html">character entity
     * references</a>. Note that this must be used with {@link #ISO8859_1_ESCAPE} to get the full list of
     * HTML 4.0 character entities.
     */
    public static final Map<CharSequence, CharSequence> HTML40_EXTENDED_ESCAPE;

    static {
        final Map<CharSequence, CharSequence> initialMap = new HashMap<>();
        // <!-- Latin Extended-B -->
        initialMap.put("ƒ", "&fnof;"); // latin small f with hook = function= florin, U+0192 ISOtech -->
        // <!-- Greek -->
        initialMap.put("Α", "&Alpha;"); // greek capital letter alpha, U+0391 -->
        initialMap.put("Β", "&Beta;"); // greek capital letter beta, U+0392 -->
        initialMap.put("Γ", "&Gamma;"); // greek capital letter gamma,U+0393 ISOgrk3 -->
        initialMap.put("Δ", "&Delta;"); // greek capital letter delta,U+0394 ISOgrk3 -->
        initialMap.put("Ε", "&Epsilon;"); // greek capital letter epsilon, U+0395 -->
        initialMap.put("Ζ", "&Zeta;"); // greek capital letter zeta, U+0396 -->
        initialMap.put("Η", "&Eta;"); // greek capital letter eta, U+0397 -->
        initialMap.put("Θ", "&Theta;"); // greek capital letter theta,U+0398 ISOgrk3 -->
        initialMap.put("Ι", "&Iota;"); // greek capital letter iota, U+0399 -->
        initialMap.put("Κ", "&Kappa;"); // greek capital letter kappa, U+039A -->
        initialMap.put("Λ", "&Lambda;"); // greek capital letter lambda,U+039B ISOgrk3 -->
        initialMap.put("Μ", "&Mu;"); // greek capital letter mu, U+039C -->
        initialMap.put("Ν", "&Nu;"); // greek capital letter nu, U+039D -->
        initialMap.put("Ξ", "&Xi;"); // greek capital letter xi, U+039E ISOgrk3 -->
        initialMap.put("Ο", "&Omicron;"); // greek capital letter omicron, U+039F -->
        initialMap.put("Π", "&Pi;"); // greek capital letter pi, U+03A0 ISOgrk3 -->
        initialMap.put("Ρ", "&Rho;"); // greek capital letter rho, U+03A1 -->
        // <!-- there is no Sigmaf, and no U+03A2 character either -->
        initialMap.put("Σ", "&Sigma;"); // greek capital letter sigma,U+03A3 ISOgrk3 -->
        initialMap.put("Τ", "&Tau;"); // greek capital letter tau, U+03A4 -->
        initialMap.put("Υ", "&Upsilon;"); // greek capital letter upsilon,U+03A5 ISOgrk3 -->
        initialMap.put("Φ", "&Phi;"); // greek capital letter phi,U+03A6 ISOgrk3 -->
        initialMap.put("Χ", "&Chi;"); // greek capital letter chi, U+03A7 -->
        initialMap.put("Ψ", "&Psi;"); // greek capital letter psi,U+03A8 ISOgrk3 -->
        initialMap.put("Ω", "&Omega;"); // greek capital letter omega,U+03A9 ISOgrk3 -->
        initialMap.put("α", "&alpha;"); // greek small letter alpha,U+03B1 ISOgrk3 -->
        initialMap.put("β", "&beta;"); // greek small letter beta, U+03B2 ISOgrk3 -->
        initialMap.put("γ", "&gamma;"); // greek small letter gamma,U+03B3 ISOgrk3 -->
        initialMap.put("δ", "&delta;"); // greek small letter delta,U+03B4 ISOgrk3 -->
        initialMap.put("ε", "&epsilon;"); // greek small letter epsilon,U+03B5 ISOgrk3 -->
        initialMap.put("ζ", "&zeta;"); // greek small letter zeta, U+03B6 ISOgrk3 -->
        initialMap.put("η", "&eta;"); // greek small letter eta, U+03B7 ISOgrk3 -->
        initialMap.put("θ", "&theta;"); // greek small letter theta,U+03B8 ISOgrk3 -->
        initialMap.put("ι", "&iota;"); // greek small letter iota, U+03B9 ISOgrk3 -->
        initialMap.put("κ", "&kappa;"); // greek small letter kappa,U+03BA ISOgrk3 -->
        initialMap.put("λ", "&lambda;"); // greek small letter lambda,U+03BB ISOgrk3 -->
        initialMap.put("μ", "&mu;"); // greek small letter mu, U+03BC ISOgrk3 -->
        initialMap.put("ν", "&nu;"); // greek small letter nu, U+03BD ISOgrk3 -->
        initialMap.put("ξ", "&xi;"); // greek small letter xi, U+03BE ISOgrk3 -->
        initialMap.put("ο", "&omicron;"); // greek small letter omicron, U+03BF NEW -->
        initialMap.put("π", "&pi;"); // greek small letter pi, U+03C0 ISOgrk3 -->
        initialMap.put("ρ", "&rho;"); // greek small letter rho, U+03C1 ISOgrk3 -->
        initialMap.put("ς", "&sigmaf;"); // greek small letter final sigma,U+03C2 ISOgrk3 -->
        initialMap.put("σ", "&sigma;"); // greek small letter sigma,U+03C3 ISOgrk3 -->
        initialMap.put("τ", "&tau;"); // greek small letter tau, U+03C4 ISOgrk3 -->
        initialMap.put("υ", "&upsilon;"); // greek small letter upsilon,U+03C5 ISOgrk3 -->
        initialMap.put("φ", "&phi;"); // greek small letter phi, U+03C6 ISOgrk3 -->
        initialMap.put("χ", "&chi;"); // greek small letter chi, U+03C7 ISOgrk3 -->
        initialMap.put("ψ", "&psi;"); // greek small letter psi, U+03C8 ISOgrk3 -->
        initialMap.put("ω", "&omega;"); // greek small letter omega,U+03C9 ISOgrk3 -->
        initialMap.put("ϑ", "&thetasym;"); // greek small letter theta symbol,U+03D1 NEW -->
        initialMap.put("ϒ", "&upsih;"); // greek upsilon with hook symbol,U+03D2 NEW -->
        initialMap.put("ϖ", "&piv;"); // greek pi symbol, U+03D6 ISOgrk3 -->
        // <!-- General Punctuation -->
        initialMap.put("•", "&bull;"); // bullet = black small circle,U+2022 ISOpub -->
        // <!-- bullet is NOT the same as bullet operator, U+2219 -->
        initialMap.put("…", "&hellip;"); // horizontal ellipsis = three dot leader,U+2026 ISOpub -->
        initialMap.put("′", "&prime;"); // prime = minutes = feet, U+2032 ISOtech -->
        initialMap.put("″", "&Prime;"); // double prime = seconds = inches,U+2033 ISOtech -->
        initialMap.put("‾", "&oline;"); // overline = spacing overscore,U+203E NEW -->
        initialMap.put("⁄", "&frasl;"); // fraction slash, U+2044 NEW -->
        // <!-- Letterlike Symbols -->
        initialMap.put("℘", "&weierp;"); // script capital P = power set= Weierstrass p, U+2118 ISOamso -->
        initialMap.put("ℑ", "&image;"); // blackletter capital I = imaginary part,U+2111 ISOamso -->
        initialMap.put("ℜ", "&real;"); // blackletter capital R = real part symbol,U+211C ISOamso -->
        initialMap.put("™", "&trade;"); // trade mark sign, U+2122 ISOnum -->
        initialMap.put("ℵ", "&alefsym;"); // alef symbol = first transfinite cardinal,U+2135 NEW -->
        // <!-- alef symbol is NOT the same as hebrew letter alef,U+05D0 although the
        // same glyph could be used to depict both characters -->
        // <!-- Arrows -->
        initialMap.put("←", "&larr;"); // leftwards arrow, U+2190 ISOnum -->
        initialMap.put("↑", "&uarr;"); // upwards arrow, U+2191 ISOnum-->
        initialMap.put("→", "&rarr;"); // rightwards arrow, U+2192 ISOnum -->
        initialMap.put("↓", "&darr;"); // downwards arrow, U+2193 ISOnum -->
        initialMap.put("↔", "&harr;"); // left right arrow, U+2194 ISOamsa -->
        initialMap.put("↵", "&crarr;"); // downwards arrow with corner leftwards= carriage return, U+21B5 NEW -->
        initialMap.put("⇐", "&lArr;"); // leftwards double arrow, U+21D0 ISOtech -->
        // <!-- ISO 10646 does not say that lArr is the same as the 'is implied by'
        // arrow but also does not have any other character for that function.
        // So ? lArr canbe used for 'is implied by' as ISOtech suggests -->
        initialMap.put("⇑", "&uArr;"); // upwards double arrow, U+21D1 ISOamsa -->
        initialMap.put("⇒", "&rArr;"); // rightwards double arrow,U+21D2 ISOtech -->
        // <!-- ISO 10646 does not say this is the 'implies' character but does not
        // have another character with this function so ?rArr can be used for
        // 'implies' as ISOtech suggests -->
        initialMap.put("⇓", "&dArr;"); // downwards double arrow, U+21D3 ISOamsa -->
        initialMap.put("⇔", "&hArr;"); // left right double arrow,U+21D4 ISOamsa -->
        // <!-- Mathematical Operators -->
        initialMap.put("∀", "&forall;"); // for all, U+2200 ISOtech -->
        initialMap.put("∂", "&part;"); // partial differential, U+2202 ISOtech -->
        initialMap.put("∃", "&exist;"); // there exists, U+2203 ISOtech -->
        initialMap.put("∅", "&empty;"); // empty set = null set = diameter,U+2205 ISOamso -->
        initialMap.put("∇", "&nabla;"); // nabla = backward difference,U+2207 ISOtech -->
        initialMap.put("∈", "&isin;"); // element of, U+2208 ISOtech -->
        initialMap.put("∉", "&notin;"); // not an element of, U+2209 ISOtech -->
        initialMap.put("∋", "&ni;"); // contains as member, U+220B ISOtech -->
        // <!-- should there be a more memorable name than 'ni'? -->
        initialMap.put("∏", "&prod;"); // n-ary product = product sign,U+220F ISOamsb -->
        // <!-- prod is NOT the same character as U+03A0 'greek capital letter pi'
        // though the same glyph might be used for both -->
        initialMap.put("∑", "&sum;"); // n-ary summation, U+2211 ISOamsb -->
        // <!-- sum is NOT the same character as U+03A3 'greek capital letter sigma'
        // though the same glyph might be used for both -->
        initialMap.put("−", "&minus;"); // minus sign, U+2212 ISOtech -->
        initialMap.put("∗", "&lowast;"); // asterisk operator, U+2217 ISOtech -->
        initialMap.put("√", "&radic;"); // square root = radical sign,U+221A ISOtech -->
        initialMap.put("∝", "&prop;"); // proportional to, U+221D ISOtech -->
        initialMap.put("∞", "&infin;"); // infinity, U+221E ISOtech -->
        initialMap.put("∠", "&ang;"); // angle, U+2220 ISOamso -->
        initialMap.put("∧", "&and;"); // logical and = wedge, U+2227 ISOtech -->
        initialMap.put("∨", "&or;"); // logical or = vee, U+2228 ISOtech -->
        initialMap.put("∩", "&cap;"); // intersection = cap, U+2229 ISOtech -->
        initialMap.put("∪", "&cup;"); // union = cup, U+222A ISOtech -->
        initialMap.put("∫", "&int;"); // integral, U+222B ISOtech -->
        initialMap.put("∴", "&there4;"); // therefore, U+2234 ISOtech -->
        initialMap.put("∼", "&sim;"); // tilde operator = varies with = similar to,U+223C ISOtech -->
        // <!-- tilde operator is NOT the same character as the tilde, U+007E,although
        // the same glyph might be used to represent both -->
        initialMap.put("≅", "&cong;"); // approximately equal to, U+2245 ISOtech -->
        initialMap.put("≈", "&asymp;"); // almost equal to = asymptotic to,U+2248 ISOamsr -->
        initialMap.put("≠", "&ne;"); // not equal to, U+2260 ISOtech -->
        initialMap.put("≡", "&equiv;"); // identical to, U+2261 ISOtech -->
        initialMap.put("≤", "&le;"); // less-than or equal to, U+2264 ISOtech -->
        initialMap.put("≥", "&ge;"); // greater-than or equal to,U+2265 ISOtech -->
        initialMap.put("⊂", "&sub;"); // subset of, U+2282 ISOtech -->
        initialMap.put("⊃", "&sup;"); // superset of, U+2283 ISOtech -->
        // <!-- note that nsup, 'not a superset of, U+2283' is not covered by the
        // Symbol font encoding and is not included. Should it be, for symmetry?
        // It is in ISOamsn -->,
        initialMap.put("⊄", "&nsub;"); // not a subset of, U+2284 ISOamsn -->
        initialMap.put("⊆", "&sube;"); // subset of or equal to, U+2286 ISOtech -->
        initialMap.put("⊇", "&supe;"); // superset of or equal to,U+2287 ISOtech -->
        initialMap.put("⊕", "&oplus;"); // circled plus = direct sum,U+2295 ISOamsb -->
        initialMap.put("⊗", "&otimes;"); // circled times = vector product,U+2297 ISOamsb -->
        initialMap.put("⊥", "&perp;"); // up tack = orthogonal to = perpendicular,U+22A5 ISOtech -->
        initialMap.put("⋅", "&sdot;"); // dot operator, U+22C5 ISOamsb -->
        // <!-- dot operator is NOT the same character as U+00B7 middle dot -->
        // <!-- Miscellaneous Technical -->
        initialMap.put("⌈", "&lceil;"); // left ceiling = apl upstile,U+2308 ISOamsc -->
        initialMap.put("⌉", "&rceil;"); // right ceiling, U+2309 ISOamsc -->
        initialMap.put("⌊", "&lfloor;"); // left floor = apl downstile,U+230A ISOamsc -->
        initialMap.put("⌋", "&rfloor;"); // right floor, U+230B ISOamsc -->
        initialMap.put("〈", "&lang;"); // left-pointing angle bracket = bra,U+2329 ISOtech -->
        // <!-- lang is NOT the same character as U+003C 'less than' or U+2039 'single left-pointing angle quotation
        // mark' -->
        initialMap.put("〉", "&rang;"); // right-pointing angle bracket = ket,U+232A ISOtech -->
        // <!-- rang is NOT the same character as U+003E 'greater than' or U+203A
        // 'single right-pointing angle quotation mark' -->
        // <!-- Geometric Shapes -->
        initialMap.put("◊", "&loz;"); // lozenge, U+25CA ISOpub -->
        // <!-- Miscellaneous Symbols -->
        initialMap.put("♠", "&spades;"); // black spade suit, U+2660 ISOpub -->
        // <!-- black here seems to mean filled as opposed to hollow -->
        initialMap.put("♣", "&clubs;"); // black club suit = shamrock,U+2663 ISOpub -->
        initialMap.put("♥", "&hearts;"); // black heart suit = valentine,U+2665 ISOpub -->
        initialMap.put("♦", "&diams;"); // black diamond suit, U+2666 ISOpub -->

        // <!-- Latin Extended-A -->
        initialMap.put("Œ", "&OElig;"); // -- latin capital ligature OE,U+0152 ISOlat2 -->
        initialMap.put("œ", "&oelig;"); // -- latin small ligature oe, U+0153 ISOlat2 -->
        // <!-- ligature is a misnomer, this is a separate character in some languages -->
        initialMap.put("Š", "&Scaron;"); // -- latin capital letter S with caron,U+0160 ISOlat2 -->
        initialMap.put("š", "&scaron;"); // -- latin small letter s with caron,U+0161 ISOlat2 -->
        initialMap.put("Ÿ", "&Yuml;"); // -- latin capital letter Y with diaeresis,U+0178 ISOlat2 -->
        // <!-- Spacing Modifier Letters -->
        initialMap.put("ˆ", "&circ;"); // -- modifier letter circumflex accent,U+02C6 ISOpub -->
        initialMap.put("˜", "&tilde;"); // small tilde, U+02DC ISOdia -->
        // <!-- General Punctuation -->
        initialMap.put("\u2002", "&ensp;"); // en space, U+2002 ISOpub -->
        initialMap.put("\u2003", "&emsp;"); // em space, U+2003 ISOpub -->
        initialMap.put("\u2009", "&thinsp;"); // thin space, U+2009 ISOpub -->
        initialMap.put("\u200C", "&zwnj;"); // zero width non-joiner,U+200C NEW RFC 2070 -->
        initialMap.put("\u200D", "&zwj;"); // zero width joiner, U+200D NEW RFC 2070 -->
        initialMap.put("\u200E", "&lrm;"); // left-to-right mark, U+200E NEW RFC 2070 -->
        initialMap.put("\u200F", "&rlm;"); // right-to-left mark, U+200F NEW RFC 2070 -->
        initialMap.put("–", "&ndash;"); // en dash, U+2013 ISOpub -->
        initialMap.put("—", "&mdash;"); // em dash, U+2014 ISOpub -->
        initialMap.put("‘", "&lsquo;"); // left single quotation mark,U+2018 ISOnum -->
        initialMap.put("’", "&rsquo;"); // right single quotation mark,U+2019 ISOnum -->
        initialMap.put("‚", "&sbquo;"); // single low-9 quotation mark, U+201A NEW -->
        initialMap.put("“", "&ldquo;"); // left double quotation mark,U+201C ISOnum -->
        initialMap.put("”", "&rdquo;"); // right double quotation mark,U+201D ISOnum -->
        initialMap.put("„", "&bdquo;"); // double low-9 quotation mark, U+201E NEW -->
        initialMap.put("†", "&dagger;"); // dagger, U+2020 ISOpub -->
        initialMap.put("‡", "&Dagger;"); // double dagger, U+2021 ISOpub -->
        initialMap.put("‰", "&permil;"); // per mille sign, U+2030 ISOtech -->
        initialMap.put("‹", "&lsaquo;"); // single left-pointing angle quotation mark,U+2039 ISO proposed -->
        // <!-- lsaquo is proposed but not yet ISO standardized -->
        initialMap.put("›", "&rsaquo;"); // single right-pointing angle quotation mark,U+203A ISO proposed -->
        // <!-- rsaquo is proposed but not yet ISO standardized -->
        initialMap.put("€", "&euro;"); // -- euro sign, U+20AC NEW -->
        HTML40_EXTENDED_ESCAPE = Collections.unmodifiableMap(initialMap);
    }

    /**
     * Reverse of {@link #HTML40_EXTENDED_ESCAPE} for unescaping purposes.
     */
    public static final Map<CharSequence, CharSequence> HTML40_EXTENDED_UNESCAPE;

    static {
        HTML40_EXTENDED_UNESCAPE = Collections.unmodifiableMap(invert(HTML40_EXTENDED_ESCAPE));
    }

    /**
     * A Map&lt;CharSequence, CharSequence&gt; to escape the basic XML and HTML
     * character entities.
     *
     * Namely: {@code " & < >}
     */
    public static final Map<CharSequence, CharSequence> BASIC_ESCAPE;

    static {
        final Map<CharSequence, CharSequence> initialMap = new HashMap<>();
        initialMap.put("\"", "&quot;"); // " - double-quote
        initialMap.put("&", "&amp;");   // & - ampersand
        initialMap.put("<", "&lt;");    // < - less-than
        initialMap.put(">", "&gt;");    // > - greater-than
        BASIC_ESCAPE = Collections.unmodifiableMap(initialMap);
    }

    /**
     * Reverse of {@link #BASIC_ESCAPE} for unescaping purposes.
     */
    public static final Map<CharSequence, CharSequence> BASIC_UNESCAPE;

    static {
        BASIC_UNESCAPE = Collections.unmodifiableMap(invert(BASIC_ESCAPE));
    }

    /**
     * A Map&lt;CharSequence, CharSequence&gt; to escape the apostrophe character to
     * its XML character entity.
     */
    public static final Map<CharSequence, CharSequence> APOS_ESCAPE;

    static {
        final Map<CharSequence, CharSequence> initialMap = new HashMap<>();
        initialMap.put("'", "&apos;"); // XML apostrophe
        APOS_ESCAPE = Collections.unmodifiableMap(initialMap);
    }

    /**
     * Reverse of {@link #APOS_ESCAPE} for unescaping purposes.
     */
    public static final Map<CharSequence, CharSequence> APOS_UNESCAPE;

    static {
        APOS_UNESCAPE = Collections.unmodifiableMap(invert(APOS_ESCAPE));
    }

    /**
     * A Map&lt;CharSequence, CharSequence&gt; to escape the Java
     * control characters.
     *
     * Namely: {@code \b \n \t \f \r}
     */
    public static final Map<CharSequence, CharSequence> JAVA_CTRL_CHARS_ESCAPE;

    static {
        final Map<CharSequence, CharSequence> initialMap = new HashMap<>();
        initialMap.put("\b", "\\b");
        initialMap.put("\n", "\\n");
        initialMap.put("\t", "\\t");
        initialMap.put("\f", "\\f");
        initialMap.put("\r", "\\r");
        JAVA_CTRL_CHARS_ESCAPE = Collections.unmodifiableMap(initialMap);
    }

    /**
     * Reverse of {@link #JAVA_CTRL_CHARS_ESCAPE} for unescaping purposes.
     */
    public static final Map<CharSequence, CharSequence> JAVA_CTRL_CHARS_UNESCAPE;

    static {
        JAVA_CTRL_CHARS_UNESCAPE = Collections.unmodifiableMap(invert(JAVA_CTRL_CHARS_ESCAPE));
    }

    /**
     * Inverts an escape Map into an unescape Map.
     *
     * @param map Map&lt;String, String&gt; to be inverted
     * @return Map&lt;String, String&gt; inverted array
     */
    public static Map<CharSequence, CharSequence> invert(final Map<CharSequence, CharSequence> map) {
        return map.entrySet().stream().collect(Collectors.toMap(Entry::getValue, Entry::getKey));
    }

}
