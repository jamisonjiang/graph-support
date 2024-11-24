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

package visual_case;

import helper.GraphvizVisual;
import java.util.HashMap;
import java.util.Map;
import org.graphper.api.Graphviz;
import org.graphper.api.Node;
import org.junit.jupiter.api.Test;

public class MultiFontDisplayTest extends GraphvizVisual {

  @Test
  public void testTop100LanguagesDisplay() {
    // Translations for "Hello, world" in the top 100 languages
    Map<String, String> languageSamples = new HashMap<>();
    languageSamples.put("English", "Hello, world");
    languageSamples.put("Mandarin Chinese", "你好，世界");
    languageSamples.put("Hindi", "नमस्ते, दुनिया");
    languageSamples.put("Spanish", "Hola, mundo");
    languageSamples.put("French", "Bonjour, monde");
    languageSamples.put("Arabic", "مرحبا بالعالم");
    languageSamples.put("Bengali", "ওহে বিশ্ব");
    languageSamples.put("Russian", "Привет, мир");
    languageSamples.put("Portuguese", "Olá, mundo");
    languageSamples.put("Urdu", "ہیلو دنیا");
    languageSamples.put("Indonesian", "Halo, dunia");
    languageSamples.put("German", "Hallo, Welt");
    languageSamples.put("Japanese", "こんにちは、世界");
    languageSamples.put("Swahili", "Salamu, dunia");
    languageSamples.put("Marathi", "नमस्कार, जग");
    languageSamples.put("Telugu", "హలో, ప్రపంచం");
    languageSamples.put("Turkish", "Merhaba, dünya");
    languageSamples.put("Tamil", "வணக்கம், உலகம்");
    languageSamples.put("Italian", "Ciao, mondo");
    languageSamples.put("Thai", "สวัสดีชาวโลก");
    languageSamples.put("Gujarati", "હેલો, વિશ્વ");
    languageSamples.put("Kannada", "ಹಲೋ, ವಿಶ್ವ");
    languageSamples.put("Persian", "سلام دنیا");
    languageSamples.put("Polish", "Witaj, świecie");
    languageSamples.put("Ukrainian", "Привіт, світ");
    languageSamples.put("Malayalam", "ഹലോ, ലോകമേ");
    languageSamples.put("Sundanese", "Halo, dunya");
    languageSamples.put("Hausa", "Sannu, duniya");
    languageSamples.put("Burmese", "မင်္ဂလာပါကမ္ဘာ");
    languageSamples.put("Oriya", "ନମସ୍କାର, ପ୍ରଥିବୀ");
    languageSamples.put("Romanian", "Salut, lume");
    languageSamples.put("Dutch", "Hallo, wereld");
    languageSamples.put("Yoruba", "Mo ki, agbaye");
    languageSamples.put("Amharic", "ሰላም ልዑል");
    languageSamples.put("Fula", "A jaaraama, aduna");
    languageSamples.put("Igbo", "Ndewo, ụwa");
    languageSamples.put("Sinhala", "හෙලෝ, ලෝකය");
    languageSamples.put("Uzbek", "Salom, dunyo");
    languageSamples.put("Zulu", "Sawubona, mhlaba");
    languageSamples.put("Hungarian", "Helló, világ");
    languageSamples.put("Tagalog", "Kamusta, mundo");
    languageSamples.put("Kinyarwanda", "Muraho, isi");
    languageSamples.put("Chewa", "Moni, dziko lapansi");
    languageSamples.put("Haitian Creole", "Bonjou, mond lan");
    languageSamples.put("Khmer", "សួស្ដីពិភពលោក");
    languageSamples.put("Shona", "Mhoro, nyika");
    languageSamples.put("Somali", "Salaan, adduun");
    languageSamples.put("Czech", "Ahoj, světe");
    languageSamples.put("Greek", "Γειά σου, κόσμε");
    languageSamples.put("Belarusian", "Прывітанне, свет");
    languageSamples.put("Kazakh", "Сәлем, әлем");
    languageSamples.put("Azerbaijani", "Salam, dünya");
    languageSamples.put("Hungarian", "Helló, világ");
    languageSamples.put("Kurdish", "Silav, cihan");
    languageSamples.put("Serbo-Croatian", "Zdravo, svijete");
    languageSamples.put("Bulgarian", "Здравей, свят");
    languageSamples.put("Finnish", "Hei, maailma");
    languageSamples.put("Danish", "Hej, verden");
    languageSamples.put("Slovak", "Ahoj, svet");
    languageSamples.put("Lithuanian", "Labas, pasauli");
    languageSamples.put("Norwegian", "Hei, verden");
    languageSamples.put("Latvian", "Sveika, pasaule");
    languageSamples.put("Estonian", "Tere, maailm");
    languageSamples.put("Basque", "Kaixo, mundua");
    languageSamples.put("Macedonian", "Здраво, свету");
    languageSamples.put("Mongolian", "Сайн уу, ертөнц");
    languageSamples.put("Tajik", "Салом, ҷаҳон");
    languageSamples.put("Armenian", "Բարեւ աշխարհ");
    languageSamples.put("Nepali", "नमस्ते, संसार");
    languageSamples.put("Pashto", "سلام نړی");
    languageSamples.put("Georgian", "გამარჯობა, მსოფლიო");
    languageSamples.put("Tigrinya", "ሰላም ዓለም");
    languageSamples.put("Bosnian", "Zdravo, svijete");
    languageSamples.put("Albanian", "Përshendetje, botë");
    languageSamples.put("Icelandic", "Halló, heimur");
    languageSamples.put("Lao", "ສະບາຍດີໂລກ");
    languageSamples.put("Luxembourgish", "Moien, Welt");
    languageSamples.put("Galician", "Ola, mundo");
    languageSamples.put("Maltese", "Hello, dinja");
    languageSamples.put("Faroese", "Hey, heimur");
    languageSamples.put("Samoan", "Talofa, lalolagi");
    languageSamples.put("Maori", "Kia ora, ao");
    languageSamples.put("Tahitian", "Ia ora na, fenua");
    languageSamples.put("Fijian", "Bula, vuravura");
    languageSamples.put("Hmong", "Nyob zoo, ntiaj teb");
    languageSamples.put("Greenlandic", "Aluu, silarsuaq");
    languageSamples.put("Wolof", "Salaamaalekum, àdduna");
    languageSamples.put("Quechua", "Rimaykullayki, pacha");
    languageSamples.put("Aymara", "Kamisa, aka pacha");
    languageSamples.put("Inuktitut", "ᐊᐃᓐᓂᐊᕐᔪᒃ, ᓯᓚ");
    languageSamples.put("Twi", "Mahalo, wiase");

    // Build Graphviz diagram with nodes for each language
    languageSamples.forEach((language, text) -> {
      Node node = Node.builder()
          .label(language + ": " + text)
          .build();
      visual(Graphviz.graph().addNode(node).build());
    });
  }
}
