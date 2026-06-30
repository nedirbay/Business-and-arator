package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiRepository {

    private val apiService = RetrofitClient.service
    private val moshi: Moshi = RetrofitClient.moshi

    suspend fun generateLesson(
        category: String,
        completedTitles: List<String>
    ): LessonJson = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        // If API key is placeholder or empty, use fallback directly
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey.startsWith("MY_")) {
            Log.w("GeminiRepository", "API Key is missing or placeholder. Using local fallback.")
            return@withContext getFallbackLesson(category, completedTitles)
        }

        val completedLessonsPrompt = if (completedTitles.isEmpty()) {
            "ulanyjy entek hiç hili sapak okamady."
        } else {
            "şu mowzuklar eýýäm okaldy we gaýtalanmaly däl: ${completedTitles.joinToString(", ")}"
        }

        val systemInstruction = """
            Sen dilerlik (sözleýiş medeniýeti, adamlara täsir ýetirmek, oratorlyk sungaty) we biznes strategiýalary boýunça professional we gyzykly emeli aň mugallymy.
            Okuwçy üçin täze we gaty peýdaly sapak döretmeli. Sapagyň dili doly TÜRKMEN dilinde bolmaly.
            Sapak gaty düşnükli, amaly mysallara baý we çuňňur bolmaly. Sapakda hökman okuwçynyň özüni barlap biljek 3 sany köp saýlawly synag (quiz) we 1 sany amaly ýumuş (practical assignment) bolmaly.
            Jogaby diňe we diňe aşakdaky JSON formatynda bermeli. Hiç hili markdown belligini (mysal üçin ```json ýa-da ```) ulanma.
            
            JSON schema:
            {
              "title": "Sapagyň ady (gysga we täsirli)",
              "introduction": "Mowzuga gysgaça gyzykly giriş we näme üçin peýdalydygy",
              "coreConcepts": ["Esasy düşünje 1", "Esasy düşünje 2", "Esasy düşünje 3"],
              "lessonText": "Sapak tekstiniň özi. Düşnükli we anyk amaly maslahatlar, tärler bolmaly (azyndan 3-4 uly abzas)",
              "practicalAssignment": "Okuwçynyň durmuşda ýa-da işinde edip biljek anyk amaly ýumşy",
              "quiz": [
                {
                  "question": "Synag soragy 1?",
                  "options": ["Jogap A", "Jogap B", "Jogap C", "Jogap D"],
                  "correctOptionIndex": 0,
                  "explanation": "Näme üçin şu jogabyň dogrudygynyň düşündirişi (Turkmen dilinde)"
                },
                {
                  "question": "Synag soragy 2?",
                  "options": ["Jogap A", "Jogap B", "Jogap C", "Jogap D"],
                  "correctOptionIndex": 1,
                  "explanation": "Düşündiriş"
                },
                {
                  "question": "Synag soragy 3?",
                  "options": ["Jogap A", "Jogap B", "Jogap C", "Jogap D"],
                  "correctOptionIndex": 2,
                  "explanation": "Düşündiriş"
                }
              ]
            }
        """.trimIndent()

        val userPrompt = """
            Kategoriýa: $category
            Täze, öň geçilmedik sapak döret.
            Mowzuk gaýtalanmasyn: $completedLessonsPrompt
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = userPrompt)))
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.8f
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("API empty response")

            Log.d("GeminiRepository", "Raw JSON: $jsonText")

            val adapter = moshi.adapter(LessonJson::class.java)
            adapter.fromJson(jsonText) ?: throw Exception("JSON conversion failed")
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Error generating from Gemini: ${e.message}. Using fallback.", e)
            getFallbackLesson(category, completedTitles)
        }
    }

    // High quality predefined fallbacks in Turkmen to ensure offline/no-key reliability
    private fun getFallbackLesson(category: String, completedTitles: List<String>): LessonJson {
        return when (category) {
            "Sözleýiş Medeniýeti" -> {
                val availableLessons = listOf(
                    LessonJson(
                        title = "Sözleýişde Dem Almagyň Syrlary",
                        introduction = "Sözlän wagtyňyz sesiňiziň titremeginiň we tiz ýadamasynyň öňüni almak üçin dogry dem almagy öwrenmek örän möhümdir.",
                        coreConcepts = listOf(
                            "Diafragma arkaly dem almak",
                            "Sesiň durnuklylygy we ritmi",
                            "Söz arasyndaky pauzalar"
                        ),
                        lessonText = "Köp adamlar gürlänlerinde ýokary gökrek dem almasyny ulanýarlar. Bu bolsa sesiň gysga bolmagyna we çalt ýadamagyna sebäp bolýar. Mugallymlar we oratorlar diafragma dem almasyny ulanmalydyrlar. Diafragma arkaly çuňňur dem alanyňyzda, sesiňiz has durlanar we güýçlener.\n\nSözlän wagtyňyz howanyňyzy tygşytly ulanmagy başarmaly. Her sözlemiň ahyrynda ýa-da möhüm pikirleriň arasynda gysga pauza alyp, ýumşak we çuňňur dem alyň. Bu diňleýjä hem aýdanlaryňyz barada oýlanmaga wagt berer.\n\nMunuň üçin her gün 5 minut dowamynda 'S' we 'Ş' seslerini uzadyp durnukly dem goýbermek maşklaryny ýerine ýetiriň. Ses durnuklylygy we ritm gürleýşiňizi has özüne çekiji eder.",
                        practicalAssignment = "Aýnanyň öňünde durup, eliňizi garna goýuň. Diňe garynyň hereket etmegi bilen 10 gezek çuňňur dem alyp, dem goýberiň. Soňra bir sözlemi ýekeje demde gaty we durnukly ses bilen aýtmaga synanyşyň.",
                        quiz = listOf(
                            QuizQuestionJson(
                                question = "Haýsy dem algy oratorlar üçin has amatly hasaplanýar?",
                                options = listOf("Gökrek dem algysy", "Diafragma dem algysy", "Çalt we gysga dem almak", "Agyzdan dem almak"),
                                correctOptionIndex = 1,
                                explanation = "Diafragma dem algysy öýkeniň doly howadan dolmagyny üpjün edip, sesiň güýçli we durnukly çykmagyna kömek edýär."
                            ),
                            QuizQuestionJson(
                                question = "Söz arasyndaky pauzalar näme üçin gerek?",
                                options = listOf("Wagt ýitirmek üçin", "Diňleýjiniň ýadamagy üçin", "Möhüm pikirleri nygtamak we täze dem almak üçin", "Söz tapman saklanmak üçin"),
                                correctOptionIndex = 2,
                                explanation = "Pauzalar diňleýjä aýdylan möhüm maglumaty özleşdirmäge kömek edýär we gürleýjä täze dem almaga mümkinçilik berýär."
                            ),
                            QuizQuestionJson(
                                question = "Sesiň titremeginiň esasy sebäbi nämedir?",
                                options = listOf("Gaty gürlemek", "Dogry dem almazlyk we gorky", "Sözleri ýalňyş aýtmak", "Örän haýal gürlemek"),
                                correctOptionIndex = 1,
                                explanation = "Dogry dem almazlyk we tolgunma/gorky ses myşsalygynyň dartylmagyna we sesiň titremegine sebäp bolýar."
                            )
                        )
                    ),
                    LessonJson(
                        title = "Täsirli Oratorlygyň 3 Esasy Sütuni",
                        introduction = "Aristoteliň ritoriýa taglymaty: Etos, Patos we Logos arkaly diňleýjileriň ýüregine we aňyna barýan ýoly tapyň.",
                        coreConcepts = listOf(
                            "Etos (Ynamlylyk we abraý)",
                            "Patos (Duýgular we emosiýalar)",
                            "Logos (Logika we deliller)"
                        ),
                        lessonText = "Täsirli çykyş etmek diňe bir gaty gürlemek däl-de, eýsem Aristoteliň 3 kadasyna eýermekdir. Birinjisi, Etos — siz diňleýjide ynam döretmelisiniz. Siziň abraýyňyz, beden diliňiz we ses tonuňyz sizi hünärmen hökmünde görkezmelidir.\n\nIkinjisi, Patos — diňleýjileriň emosiýalaryna täsir etmek. Diňe sanlar we deliller adamlary gürledip bilmez. Hakyky hekaýalar (storytelling), gyzykly mysallar we adamlaryň duýgularyna degýän pikirler çykyşyňyzy janlandyrar.\n\nÜçünjisi bolsa Logos — logiki taýdan dogry we esaslandyrylan çykyş. Siziň aýdýan maglumatlaryňyz yzygiderli, subutnamaly we akyla laýyk bolmalydyr. Şunda bu 3 sütün birleşende çykyşyňyz kämil bolar.",
                        practicalAssignment = "Isleseňiz öz dostuňyza täze bir teklibi hödürläň. Şol teklipde ilki öz ynamlylygyňyzy (Etos), soňra teklibiň peýdasyny hekaýa arkaly (Patos) we soňunda logiki deliller bilen (Logos) gysgaça gürrüň beriň.",
                        quiz = listOf(
                            QuizQuestionJson(
                                question = "Etos nämäni aňladýar?",
                                options = listOf("Logiki subutnamalary", "Diňleýjileriň duýgularyny", "Çykyş edýäniň ynamlylygyny we abraýyny", "Wagty doly ulanmagy"),
                                correctOptionIndex = 2,
                                explanation = "Etos - bu çykyş edýän adamyň özüne bolan ynamy, abraýy we hünärmenlik derejesidir."
                            ),
                            QuizQuestionJson(
                                question = "Hekaýa aýtmak (storytelling) haýsy sütüne degişli?",
                                options = listOf("Logos", "Patos", "Etos", "Heňňem"),
                                correctOptionIndex = 1,
                                explanation = "Hekaýalar we mysallar adamlarda duýgy (emosiýa) oýarýandygy sebäpli olar Patos sütunine degişlidir."
                            ),
                            QuizQuestionJson(
                                question = "Haýsy sütün logiki delillere we sanlara daşanýar?",
                                options = listOf("Logos", "Etos", "Patos", "Ählisi"),
                                correctOptionIndex = 0,
                                explanation = "Logos akyla laýyklyk, yzygiderlilik, faktlar we deliller esasynda guralýan sütündir."
                            )
                        )
                    )
                )
                // Filter out already completed if possible, otherwise return first
                availableLessons.firstOrNull { it.title !in completedTitles } ?: availableLessons.random()
            }
            "Adamlara Täsir Ýetirmek" -> {
                val availableLessons = listOf(
                    LessonJson(
                        title = "Robert Çialdininiň 'Özaralyk' Prinsipi",
                        introduction = "Adamlar özlerine ýagşylyk eden kişilere jogap hökmünde ýagşylyk etmäge meýillidirler. Bu täsir etmegiň iň güýçli guralydyr.",
                        coreConcepts = listOf(
                            "Özaralyk (Reciprocity) düzgüni",
                            "Ilki bermek, soňra soramak",
                            "Yhlaslylyk we garaşsyzlyk"
                        ),
                        lessonText = "Psiholog Robert Çialdininiň aýtmagyna görä, adamzat jemgyýetinde iň güýçli kadalaryň biri 'özaralyk' kadasydyr. Eger kimdir biri bize kömek etse ýa-da sowgat berse, biz özümizi şol adama borçly ýaly duýýarys we mümkinçilik bolanda jogap bermäge çalyşýarys.\n\nTäsir etmek we biznes strategiýasynda bu düzgün şeýle ulanylýar: ilki bilen garşyňyzdaky adama gymmatly maglumat, goldaw ýa-da sowgat hödürläň, ýöne munuň üçin hiç zat talap etmäň. Bu gural ynam döredýär we soňra siz kömek ýa-da hyzmatdaşlyk soranyňyzda garşyňyzdaky adam 'ýok' diýmäge gaty kynlyk çeker.\n\nSowgadyňyzyň ýa-da kömegiňiziň garaşylmadyk we şahsy bolmagy onuň täsirini has-da güýçlendirýär. Emma munuň ak ýürekden we yhlasly bolmagy möhümdir.",
                        practicalAssignment = "Bu gün işde ýa-da okuwda bir kärdeşiňize garaşylmadyk goldaw beriň (meselem, kofe alyp beriň ýa-da kyn işinde kömek ediň). Olaryň reaksiýasyna we geljekde size bolan gatnaşygyna syn ediň.",
                        quiz = listOf(
                            QuizQuestionJson(
                                question = "Robert Çialdininiň 'Özaralyk' prinsipi nämä esaslanýar?",
                                options = listOf("Adamlary gorkuzmaga", "Bize ýagşylyk edene ýagşylyk bilen jogap bermek islegine", "Iň arzan zady satmaga", "Sözlemegi bes etmäge"),
                                correctOptionIndex = 1,
                                explanation = "Özaralyk kadasy adamlaryň özüne kömek ýa-da peşgeş beren adama borçly bolup, jogap ýagşylygyny etmek meýlidir."
                            ),
                            QuizQuestionJson(
                                question = "Özaralyk kadatynyň güýjüni näme has-da artdyrýar?",
                                options = listOf("Sowgadyň gymmat bolmagy", "Sowgadyň garaşylmadyk we şahsy bolmagy", "Hiç zat bermezlik", "Gaty sesiň bilen talap etmek"),
                                correctOptionIndex = 1,
                                explanation = "Garaşylmadyk we şahsy peşgeşler adamlarda has uly minnetdarlyk duýgusyny we täsiri oýarýar."
                            ),
                            QuizQuestionJson(
                                question = "Bu prinsipi ulananyňyzda haýsy ýalňyşlyk täsiri ýok edip biler?",
                                options = listOf("Ilki kömek edip, soňra munuň üçin derrew pul talap etmek", "Ýumşak ses bilen geplemek", "Uzak garaşmak", "Parahat durmak"),
                                correctOptionIndex = 0,
                                explanation = "Eger beren derrew ýagşylygyňyz üçin peýda talap etseňiz, adamlar munuň manipulýasiýadygyny duýarlar we ynam ýiter."
                            )
                        )
                    ),
                    LessonJson(
                        title = "Sözlemezden Täsir Etmek: Beden Dili",
                        introduction = "Çykyşyňyzyň ýa-da gepleşigiňiziň 55%-i beden dili arkaly diňleýjä ýetirilýär. Dogry beden dili ynamyň açarydyr.",
                        coreConcepts = listOf(
                            "Açyk beden pozalary",
                            "Göz gatnaşygy (Eye contact)",
                            "Beden hereketleriniň simmetriýasy"
                        ),
                        lessonText = "Gepleşenimizde ýa-da çykyş edenimizde aýdýan sözlerimizden has möhüm zat bar — ol hem biziň beden dilimizdir. Gollaryňyzy gökregiňizde gowşuryp durmak (closed posture) garşyňyzdaky adama siziň goranyşdadygyňyzy ýa-da pikirlere ýapykdygyňyzy görkezýär. Elmydama açyk beden duruşlaryny saýlaň.\n\nGöz gatnaşygy ynamyň we gyzyklanmanyň baş şertidir. Gepleşýän adamyňyzyň gözüne wagtal-wagtal serediň (gaty synlap durman, ýumşak göz gatnaşygyny saklaň). Bu siziň ynamlydygyňyzy we garşyňyzdakyny diňleýändigiňizi görkezer.\n\nŞeýle-de, gürleýän adamyňyzyň beden hereketlerini azajyk we tebigy görnüşde gaýtalamak (mirroring) aňasty derejede aragatnaşygy we ynamy berkidýär.",
                        practicalAssignment = "Geljekki gepleşigiňizde gollaryňyzy gowşurman, açyk beden duruşyny saklaň, adamyň gözüne ýumşaklyk bilen serediň we onuň beden dili hereketlerini tebigy usulda aňasty gaýtalamaga synanyşyň.",
                        quiz = listOf(
                            QuizQuestionJson(
                                question = "Gollaryňyzy gökregiňizde gowşuryp durmak nämäni aňladyp biler?",
                                options = listOf("Örän ynamlylygy", "Goranyş, ynamyň pesligi ýa-da ýapyklygy", "Mowzugy gowy bilýändigiňizi", "Uklap galmak islegi"),
                                correctOptionIndex = 1,
                                explanation = "Kolluň gowşurylmagy beden dilinde ýapyklyk, goranyş we söhbete gyzyklanmazlyk hökmünde kabul edilýär."
                            ),
                            QuizQuestionJson(
                                question = "Aragatnaşykda aňasty ynamy döretmek üçin haýsy tär ulanylýar?",
                                options = listOf("Gaty gykylyk etmek", "Aňasty meňzeşlik (mirroring)", "Hiç ýere seretmezlik", "Tiz-tizden gürlemek"),
                                correctOptionIndex = 1,
                                explanation = "Mirroring (söhbetdeşiň hereketlerini ýumşak gaýtalamak) adamda 'özüm ýaly ynamly adam' duýgusyny tebigy oýarýar."
                            ),
                            QuizQuestionJson(
                                question = "Göz gatnaşygy nähili bolmaly?",
                                options = listOf("Adamy gorkuzar ýaly gaty seretmeli", "Hiç wagt gözüne seretmeli däl", "Tebigy, ýumşak we wagtal-wagtal göz gatnaşygy saklanmaly", "Diňe ýere seretmeli"),
                                correctOptionIndex = 2,
                                explanation = "Tebigy we ýumşak göz gatnaşygy garşyňyzdaky adama hormat goýýandygyňyzy we ynamyňyzy görkezýär."
                            )
                        )
                    )
                )
                availableLessons.firstOrNull { it.title !in completedTitles } ?: availableLessons.random()
            }
            "Biznes Strategiýalary" -> {
                val availableLessons = listOf(
                    LessonJson(
                        title = "Lean Startup: Çalt Synag we Ösüş",
                        introduction = "Milliardlarça pul ýitirmezden öň önümiňizi iň kiçi görnüşde (MVP) bazarda synap görmegi we çalt ösmegi öwreniň.",
                        coreConcepts = listOf(
                            "MVP (Minimum Viable Product)",
                            "Gurmak-Ölçemek-Öwrenmek aýlawy",
                            "Pivot (Strategiýany üýtgetmek)"
                        ),
                        lessonText = "Köp biznesler uly maýa goýup, ýyllar boýunça önüm taýýarlaýarlar we ahyrda önümiň hiç kime gerek däldigini bilip galýarlar. 'Lean Startup' usuly munuň öňüni alýar. Ilki bilen önümiň iň esasy aýratynlygy bolan iň kiçi görnüşini — MVP-ni taýýarlaň.\n\nBu MVP-ni derrew hakyky müşderilere hödürläň. Onuň ulanylyşyny ölçäň, pikirlere we nätanyş adamlaryň reaksiyalaryna syn ediň. Şonuň esasynda öwrenip, önümi kämilleşdiriň ýa-da ugruny doly üýtgediň (Pivot).\n\nBu aýlaw näçe çalt aýlansa, biznesiňiz şonça çalt hakyky peýda getirer we az çykdajy bilen guralar. Startup-lar üçin çaltlyk we çeýelik iň möhüm aýratynlykdyr.",
                        practicalAssignment = "Kelleňizdäki bir biznes ideýany alyň. Ony iň az çykdajy we iň gysga wagtda (mysal üçin 1 günde we 0 manat harçlap) nädip müşderilere hödürläp biljekdigiňizi (MVP) kagyza ýazyň.",
                        quiz = listOf(
                            QuizQuestionJson(
                                question = "MVP näme diýmekdir?",
                                options = listOf("Örän gymmat önüm", "Iň çylşyrymly we kämil programma", "Iň kiçi we esasy gymmatlygy hödürleýän synag önümi", "Biznesiň binasy"),
                                correctOptionIndex = 2,
                                explanation = "MVP (Minimum Viable Product) iň az çykdajy bilen bazaryň islegini synap görmek üçin taýýarlanan başlangyç önümdir."
                            ),
                            QuizQuestionJson(
                                question = "Lean Startup aýlawy haýsydyr?",
                                options = listOf("Planlaşdyrmak-Garaşmak-Gyzgyn ulanmak", "Gurmak-Ölçemek-Öwrenmek", "Satmak-Pul gazanmak-Harçlamak", "Müşderi tapmak-Reklama bermek"),
                                correctOptionIndex = 1,
                                explanation = "Lean Startup 'Gurmak (önüm ýasamak), Ölçemek (faktlary we pikirleri ýygnamak), Öwrenmek (geljekki ädimi kesgitlemek)' aýlawyna daşanýar."
                            ),
                            QuizQuestionJson(
                                question = "Pivot etmek nämäni aňladýar?",
                                options = listOf("Önümi satmagy bes etmek", "Şereketi satmak", "Müşderileriň islegine görä biznes strategiýasyny we ugruny üýtgetmek", "Bankrot bolmak"),
                                correctOptionIndex = 2,
                                explanation = "Pivot - synaglaryň netijesinde asyl ideýanyň işlemändigini görüp, guraly we ugry üýtgetmekdir."
                            )
                        )
                    ),
                    LessonJson(
                        title = "SWOT Analizi: Özüňi we Bäsdeşleri Tanatmak",
                        introduction = "SWOT analizi biznesiň we şahsyýetiň güýçli, gowşak taraplaryny, mümkinçiliklerini we howplaryny kesgitlemegiň iň täsirli usulydyr.",
                        coreConcepts = listOf(
                            "Güýçli taraplar (Strengths)",
                            "Gowşak taraplar (Weaknesses)",
                            "Mümkinçilikler (Opportunities)",
                            "Howplar (Threats)"
                        ),
                        lessonText = "SWOT analizi — bu islendik taslama ýa-da karara baha bermek üçin amatly guraldyr. Ol dört bölekden ybaratdyr. Güýçli we gowşak taraplar bu içki şertlerdir (siz üýtgedip bilýäniňiz), Mümkinçilikler we Howplar bolsa daşky şertlerdir (bazardaky ýagdaýlar).\n\nÖz güýçli taraplaryňyzy bilmek size bäsdeşlikde öňe saýlanmaga kömek eder. Gowşak taraplaryňyzy bolsa anyklap, olary gowulandyrmaga ýa-da zyýanyny azaltmaga çalyşmalysydyr.\n\nDaşky gurşawdaky täze tehnologiýalar ýa-da islegler mümkinçilikler bolup biler, emma ykdysady üýtgeşmeler ýa-da täze bäsdeşler bolsa howplar hasaplanýar. SWOT analizi strategiýany dogry gurnamaga ýol görkezýär.",
                        practicalAssignment = "Özüňiz ýa-da kiçijik biznes ideýaňyz üçin SWOT analizi tablisasyny gurnap, her bölek üçin azyndan 2 sany madda ýazyň.",
                        quiz = listOf(
                            QuizQuestionJson(
                                question = "SWOT analizinde haýsy şertler içki faktorlar hasaplanýar?",
                                options = listOf("Mümkinçilikler we Howplar", "Güýçli we Gowşak taraplar", "Diňe Howplar", "Ählisi"),
                                correctOptionIndex = 1,
                                explanation = "Güýçli we gowşak taraplar siziň özüňize ýa-da şereketinize bagly bolan içki şertlerdir."
                            ),
                            QuizQuestionJson(
                                question = "Täze ýüze çykan tehnologiýa SWOT-yň haýsy bölegine degişli?",
                                options = listOf("Gowşak taraplar", "Howplar", "Mümkinçilikler", "Güýçli taraplar"),
                                correctOptionIndex = 2,
                                explanation = "Täze tehnologiýalar daşky gurşawda ýüze çykyp, ösmek we täze önüm ýasamak üçin Mümkinçilik (Opportunities) döredýär."
                            ),
                            QuizQuestionJson(
                                question = "Howplar (Threats) nämäni aňladýar?",
                                options = listOf("Içki işgärleriň sanyny", "Bize zeper ýetirip biljek daşky faktorlary we bäsdeşleri", "Satylmadyk harytlary", "Müşderiniň razylygyny"),
                                correctOptionIndex = 1,
                                explanation = "Howplar - siziň gözegçiligiňizden daşary bolan, ýöne taslama zyýan berip biljek daşky bäsdeşler ýa-da kanuny üýtgeşmelerdir."
                            )
                        )
                    )
                )
                availableLessons.firstOrNull { it.title !in completedTitles } ?: availableLessons.random()
            }
            else -> {
                LessonJson(
                    title = "Şahsy Ösüşiň Esaslary",
                    introduction = "Gündelik kiçijik ädimler bilen uly üstünliklere ýetiň.",
                    coreConcepts = listOf("Yzygiderlilik", "Maşklanmak", "Gyzyklanmak"),
                    lessonText = "Gündelik ösüş islendik ugurda üstünlik gazanmagyň esasy açarydyr. Her gün täze zat öwrenmek we ony iş ýüzünde ulanmak sizi gysga wagtda professional derejä ýetirer.",
                    practicalAssignment = "Şu gün öwrenen täze tärleriňizi 1 gezek durmuşda ulanyň.",
                    quiz = listOf(
                        QuizQuestionJson(
                            question = "Ösüşiň esasy şerti näme?",
                            options = listOf("Yzygiderlilik", "Çalt taşlamak", "Garaşmak", "Hiç zat etmezlik"),
                            correctOptionIndex = 0,
                            explanation = "Yzygiderlilik - her gün az-azdan hem bolsa yzygiderli dowam etmek iň uly netijeleri berýär."
                        )
                    )
                )
            }
        }
    }
}
