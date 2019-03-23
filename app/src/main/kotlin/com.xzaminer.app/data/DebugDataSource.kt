package com.xzaminer.app.data

import android.net.Uri
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.simplemobiletools.commons.extensions.getInternalStoragePath
import com.xzaminer.app.SimpleActivity
import com.xzaminer.app.billing.Purchase
import com.xzaminer.app.category.Category
import com.xzaminer.app.course.Course
import com.xzaminer.app.course.CourseSection
import com.xzaminer.app.studymaterial.Question
import com.xzaminer.app.studymaterial.QuestionOption
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.studymaterial.Video
import com.xzaminer.app.utils.*
import java.io.File


class DebugDataSource {
    fun initMockDataRealtimeDatabase(dataSource: DataSource) {
        val dbRef = dataSource.getCatsDatabase()

        val c1 = Category(1, "Professional Courses", "Professional Courses",
            "images/cat_1.png", getSubCategories(1), getCourses(1))
        dbRef.child("cats").setValue(linkedMapOf(c1.id.toString() to c1))
        dbRef.push()
    }

    private fun getCourses(id: Int): HashMap<String, Course>? {
        if (id == 1) {
            val c1 = Course(
                101,
                "CISA\n(Certified Information System Auditor)", "Structured Courseware to Pass the CISA Exam having Question Banks that builds confidence, Videos, Question Banks & Flash Cards",
                "images/cat_2.png",
                getSections(101), getPurchaseInfo(101), arrayListOf("courses/101/desc_1.jpg", "courses/101/desc_2.jpg", "courses/101/desc_3.jpg", "courses/101/desc_4.jpg", "courses/101/desc_5.jpg"),
                "CISA"
            )
            return linkedMapOf(c1.id.toString() to c1)
        }
        return linkedMapOf()
    }

    private fun getSections(id: Int): HashMap<String, CourseSection> {
        if (id == 101) {
//            val s1 = CourseSection(
//                1011,
//                "31 Days Course (Concepts)",
//                "",
//                "", getConcepts(1011), STUDY_MATERIAL_TYPE_STUDY_MATERIAL,
//                arrayListOf(), 2
//            )
            val s2 = CourseSection(
                1012,
                "31 Days Course (Questions)",
                "",
                "", getQuestionBanks(1012), STUDY_MATERIAL_TYPE_QUESTION_BANK,
                getPurchaseInfo(1012), 3, true
            )
//            val s3 = CourseSection(
//                1013,
//                "Review Manuals",
//                "",
//                "", getReviewManuals(1013), STUDY_MATERIAL_TYPE_STUDY_MATERIAL,
//                arrayListOf(), 6
//            )
            val s4 = CourseSection(
                1014,
                "Flash Cards",
                "",
                "", getFlashCards(1014), STUDY_MATERIAL_TYPE_STUDY_MATERIAL,
                getPurchaseInfo(1014), 5
            )
            val s5 = CourseSection(
                1015,
                "Question Banks",
                "",
                "", getQuestionBanks(1015), STUDY_MATERIAL_TYPE_QUESTION_BANK,
                getPurchaseInfo(1015), 4
            )
            val s6 = CourseSection(
                1016,
                "Videos",
                "",
                "", getVideosSection(1016), STUDY_MATERIAL_TYPE_VIDEO,
                getPurchaseInfo(1016), 1, true
            )
            val s7 = CourseSection(
                1017,
                "31 Days Course",
                "",
                "", getVideosSection(1017), STUDY_MATERIAL_TYPE_VIDEO,
                getPurchaseInfo(1017), 2, true
            )
            return hashMapOf(s2.id.toString() to s2, s4.id.toString() to s4, s5.id.toString() to s5, s6.id.toString() to s6, s7.id.toString() to s7)
//            return linkedMapOf(s1.id.toString() to s1, s2.id.toString() to s2, s3.id.toString() to s3, s4.id.toString() to s4, s5.id.toString() to s5, s6.id.toString() to s6)
        }
        return hashMapOf()
    }

    private fun getPurchaseInfo(id: Int): ArrayList<Purchase> {
        if (id == 101) {
            return arrayListOf(Purchase(PURCHASE_COURSE_IAP+"101", "CISA Course", "Purchase entire course including 65 Videos + 41 Question Banks + 5 Flash Cards for 50% discount",
                PURCHASE_TYPE_IAP, "3500", "7000", true, null, null, "Entire Course CISA"))
        } else if (id == 1012) {
            return arrayListOf(Purchase(PURCHASE_SECTION_IAP+"1012", "CISA 31 Day Question Banks","Purchase entire Question Banks section including 31 Question Banks for 50% discount",
                PURCHASE_TYPE_IAP, "1000", "", true, null, null, "Entire Section of Course: CISA"))
        } else if (id == 1014) {
            return arrayListOf(Purchase(PURCHASE_SECTION_IAP+"1014", "CISA Flash Cards","Purchase entire Flash Cards section including 5 Flash Cards for 50% discount",
                PURCHASE_TYPE_IAP, "1000", "", true, null, null, "Entire Section of Course: CISA"))
        } else if (id == 1015) {
            return arrayListOf(Purchase(PURCHASE_SECTION_IAP+"1015", "CISA Question Banks","Purchase entire Question Banks section including 10 Question Banks for 50% discount",
                PURCHASE_TYPE_IAP, "2000", "", true, null, null, "Entire Section of Course: CISA"))
        } else if (id == 1016) {
            return arrayListOf(Purchase(PURCHASE_SECTION_IAP+"1016", "CISA Videos", "Purchase entire Videos section including 5 domains and each domain having 5 videos each",
                PURCHASE_TYPE_IAP, "2000", "", true, null, null, "Entire Section of Course: CISA"))
        } else if (id == 1017) {
            return arrayListOf(Purchase(PURCHASE_SECTION_IAP+"1017", "CISA 31 Days Course","Purchase entire 31 Days Course section including Video for each day",
                PURCHASE_TYPE_IAP, "1000", "", true, null, null, "Entire Section of Course: CISA"))
        } else if (id == 101401) {
            return arrayListOf(Purchase(PURCHASE_SECTION_STUDY_MATERIAL+"101401", "CISA Review Manual - Domain 01","Purchase review manual for Domain 01",
                PURCHASE_TYPE_IAP, "100", "", true, null, null, "Study Material of Section: CISA Review Manual of Course: CISA"))
        } else if (id == 101204) {
            return arrayListOf(Purchase(PURCHASE_SECTION_STUDY_MATERIAL+"101204", "CISA Question Banks - Question Bank 01","Purchase CISA Question Banks - Question Bank 01",
                PURCHASE_TYPE_IAP, "100", "", true, null, null, "Question Bank of Section: CISA Question Banks of Course: CISA"))
        } else if (id == 101402) {
            return arrayListOf(Purchase(PURCHASE_SECTION_STUDY_MATERIAL+"101402", "CISA Review Manual - Domain 02","Purchase review manual for Domain 02",
                PURCHASE_TYPE_IAP, "100", "", true, null, null, "Study Material of Section: CISA Review Manual of Course: CISA"))
        } else if (id == 101600) {
            return arrayListOf(Purchase(PURCHASE_SECTION_IAP+"101600", "CISA Videos - Training","Purchase videos for Training",
                PURCHASE_TYPE_IAP, "100", "", true, null, null, "Entire Section of Course: CISA"))
        } else if (id == -1) {
            return arrayListOf(Purchase(PURCHASE_SECTION_IAP+"-1", "CISA Videos - Training","Purchase videos for Training",
                PURCHASE_TYPE_TRIAL, "100", "", true, null, null, "Entire Section of Course: CISA"))
        }
        return arrayListOf()
    }

    private fun getVideosSection(id: Int): HashMap<String, StudyMaterial> {
        if (id == 1016) {
            var counter = 2
            val videos = hashMapOf<String, StudyMaterial>()
            for (i in 101601..101605) {
                val c = StudyMaterial(
                    i.toLong(),
                    "Domain 0" + (i - 101600),
                    "",
                    "courses/101/title_01_h_0" + (i - 101600) + ".jpg", 0, getStudyMaterialProperties(),
                     getQuestions(i), getVideos(i), getPurchaseInfo(i), "", STUDY_MATERIAL_TYPE_VIDEO, counter++
                )
                videos[c.id.toString()] = c
            }
            val c = StudyMaterial(
                101600,
                "Trial",
                "",
                "courses/101/title_01_h_00_trial.jpg", 0, getStudyMaterialProperties(),
                getQuestions(101600), getVideos(101600), getPurchaseInfo(-1), "", STUDY_MATERIAL_TYPE_VIDEO, 1
            )
            videos[c.id.toString()] = c
            return videos
        }
        if (id == 1017) {
            var counter = 1
            val videos = hashMapOf<String, StudyMaterial>()
            val c = StudyMaterial(
                101700,
                "Trial",
                "",
                "courses/101/title_02_h_00_trial.jpg", 0, getStudyMaterialProperties(),
                getQuestions(101700), getVideos(101700), getPurchaseInfo(-1), "", STUDY_MATERIAL_TYPE_VIDEO, counter++
            )
            videos[c.id.toString()] = c
            val c1 = StudyMaterial(
                101701,
                "Day 1-7",
                "",
                "courses/101/title_02_h_01.jpg", 0, getStudyMaterialProperties(),
                getQuestions(101701), getVideos(101701), getPurchaseInfo(101701), "", STUDY_MATERIAL_TYPE_VIDEO, counter++
            )
            videos[c1.id.toString()] = c1
            val c2 = StudyMaterial(
                101708,
                "Day 8-14",
                "",
                "courses/101/title_02_h_02.jpg", 0, getStudyMaterialProperties(),
                getQuestions(101708), getVideos(101708), getPurchaseInfo(101708), "", STUDY_MATERIAL_TYPE_VIDEO, counter++
            )
            videos[c2.id.toString()] = c2
            val c3 = StudyMaterial(
                101715,
                "Day 15-21",
                "",
                "courses/101/title_02_h_03.jpg", 0, getStudyMaterialProperties(),
                getQuestions(101715), getVideos(101715), getPurchaseInfo(101715), "", STUDY_MATERIAL_TYPE_VIDEO, counter++
            )
            videos[c3.id.toString()] = c3
            val c4 = StudyMaterial(
                101722,
                "Day 22-28",
                "",
                "courses/101/title_02_h_04.jpg", 0, getStudyMaterialProperties(),
                getQuestions(101722), getVideos(101722), getPurchaseInfo(101722), "", STUDY_MATERIAL_TYPE_VIDEO, counter++
            )
            videos[c4.id.toString()] = c4
            val c5 = StudyMaterial(
                101729,
                "Day 29-31",
                "",
                "courses/101/title_02_h_05.jpg", 0, getStudyMaterialProperties(),
                getQuestions(101729), getVideos(101729), getPurchaseInfo(101729), "", STUDY_MATERIAL_TYPE_VIDEO, counter++
            )
            videos[c5.id.toString()] = c5

            return videos
        }
        return hashMapOf()
    }

    private fun getVideos(id: Int): ArrayList<Video> {
        if(id in 101601..101605) {
            val videos = arrayListOf<Video>()
            for (i in 1016001..1016005) {
                val c = Video(
                    i.toLong(),
                    "Video 0" + (i - 1016000),
                    "This video covers the details of Certified Information systems Auditor in detail",
                    "", "video_101601_" + (i - 1016000) + ".mp4", "courses/101/", "", (i - 1016000), "08:23"
                )
                videos.add(c)
            }
            return videos
        }
        if(id in 101701..101729) {
            val videos = arrayListOf<Video>()
            var i = 0
            while(i < 7 && ((id - 101729 + i) < 3)) {
                val c = Video(
                    i.toLong(),
                    "Day " + (id + i + 1 - 101701),
                    "This video covers the details of Day for course Certified Information systems Auditor",
                    "", "video_101601_" + (i + 1) + ".mp4", "courses/101/", "", (i + 1), "08:23"
                )
                videos.add(c)
                i++
            }
            return videos
        }
        if(id == 101600 || id == 101700) {
            val videos = arrayListOf<Video>()
            for (i in 1016001..1016005) {
                val c = Video(
                    i.toLong(),
                    "Video 0" + (i - 1016000),
                    "This video covers the details of Certified Information systems Auditor in detail",
                    "", "video_101601_" + (i - 1016000) + ".mp4", "courses/101/", "", (i - 1016000), "08:23"
                )
                videos.add(c)
            }
            return videos
        }
        if(id== 101400) {
            return arrayListOf(Video(
                10140001,
                "Audio 01",
                "This Audio covers the details of this question",
                "", "audio_101501_01.mp3", "courses/101/", "", 1, "04:23"
            ))
        }
        return arrayListOf()
    }

    private fun getStudyMaterialProperties(): HashMap<String, ArrayList<String>> {
        return hashMapOf()
    }

    private fun getFlashCards(id: Int): LinkedHashMap<String, StudyMaterial> {
        if (id == 1014) {
            var counter = 1
            val c = StudyMaterial(
                101400,
                "Trial",
                "Trial",
                "courses/101/title_05_h_00_trial.jpg", 0, linkedMapOf(),
                getQuestions(101400), arrayListOf(), getPurchaseInfo(-1), "", STUDY_MATERIAL_TYPE_STUDY_MATERIAL, counter++
            )
            val c1 = StudyMaterial(
                101401,
                "Domain 01",
                "Domain 01",
                "courses/101/title_05_h_01.jpg", 0, linkedMapOf(),
                getQuestions(101401), arrayListOf(), getPurchaseInfo(101401), "", STUDY_MATERIAL_TYPE_STUDY_MATERIAL, counter++
            )
            val flashCards = linkedMapOf(c1.id.toString() to c1, c.id.toString() to c)
            for (i in 101402..101405) {
                val c = StudyMaterial(
                    i.toLong(),
                    "Domain 0" + (i - 101400),
                    "Domain 0" + (i - 101400),
                    "courses/101/title_05_h_0" + (i - 101400) + ".jpg", 0, linkedMapOf(),
                    getQuestions(i), arrayListOf(), getPurchaseInfo(i), "", STUDY_MATERIAL_TYPE_STUDY_MATERIAL, counter++
                )
                flashCards[c.id.toString()] = c
            }
            return flashCards
        }
        return linkedMapOf()
    }

    private fun getReviewManuals(id: Int): LinkedHashMap<String, StudyMaterial> {
        if (id == 1013) {
            val c2 = StudyMaterial(
                101302,
                "Domain 02",
                "Domain 02",
                "images/cisa/rm_2.jpg", 0, linkedMapOf(),
                getQuestions(101302), arrayListOf(), arrayListOf(), ""
            )
            val c3 = StudyMaterial(
                101303,
                "Domain 03",
                "Domain 03",
                "images/cisa/rm_3.jpg", 0, linkedMapOf(),
                getQuestions(101303), arrayListOf(), arrayListOf(), ""
            )
            val c4 = StudyMaterial(
                101304,
                "Domain 04",
                "Domain 04",
                "images/cisa/rm_4.jpg", 0, linkedMapOf(),
                getQuestions(101304), arrayListOf(), arrayListOf(), ""
            )
            val c5 = StudyMaterial(
                101305,
                "Domain 05",
                "Domain 05",
                "images/cisa/rm_5.jpg", 0, linkedMapOf(),
                getQuestions(101305), arrayListOf(), arrayListOf(), ""
            )
            return linkedMapOf(c2.id.toString() to c2, c3.id.toString() to c3, c4.id.toString() to c4, c5.id.toString() to c5)
        }
        return linkedMapOf()
    }

    private fun getConcepts(id: Int): LinkedHashMap<String, StudyMaterial> {
        if (id == 1011) {
            val c1 = StudyMaterial(
                101133,
                "CISA 30 Days Course (Concepts)",
                "CISA 30 Days Course (Concepts)",
                "images/cisa/co_1.jpg", 0, linkedMapOf(),
                getQuestions(1011), arrayListOf(), arrayListOf(), ""
            )
            val studyMats = linkedMapOf(c1.id.toString() to c1)
            for (i in 101102..101132) {
                val c = StudyMaterial(
                    i.toLong(),
                    "Day " + (i - 101101),
                    "Day " + (i - 101101),
                    "images/cisa/co_" + (i - 101101) + ".jpg", 0, linkedMapOf(),
                    getQuestions(i), arrayListOf(), arrayListOf(), ""
                )
                studyMats[c.id.toString()] = c
            }
            return studyMats
        }
        return linkedMapOf()
    }

    private fun getQuestionBanks(id: Int): LinkedHashMap<String, StudyMaterial> {
        if (id == 1012) {
            var counter = 4
            val questionBanks = linkedMapOf<String, StudyMaterial>()
            for (i in 101204..101212) {
//                Log.d("Xz", "creating "+i+"...")
                val c = StudyMaterial(
                    i.toLong(),
                    "Day " + (i - 101203),
                    "Day " + (i - 101203),
                    "courses/101/title_03_h_0" + (i - 101203) + ".jpg", 0, linkedMapOf(),
                    getQuestions(i), arrayListOf(), getPurchaseInfo(i), "", STUDY_MATERIAL_TYPE_QUESTION_BANK, counter++
                )
                questionBanks[c.id.toString()] = c
            }
            for (i in 101213..101234) {
//                Log.d("Xz", "creating "+i+"...")
                val c = StudyMaterial(
                    i.toLong(),
                    "Day " + (i - 101203),
                    "Day " + (i - 101203),
                    "courses/101/title_03_h_" + (i - 101203) + ".jpg", 0, linkedMapOf(),
                    getQuestions(i), arrayListOf(), getPurchaseInfo(i), "", STUDY_MATERIAL_TYPE_QUESTION_BANK, counter++
                )
                questionBanks[c.id.toString()] = c
            }
            val c = StudyMaterial(
                101200,
                "Trial",
                "Trial",
                "courses/101/title_03_h_00_trial.jpg", 0, linkedMapOf(),
                getQuestions(101200), arrayListOf(), getPurchaseInfo(-1), "", STUDY_MATERIAL_TYPE_QUESTION_BANK, 3
            )
            questionBanks[c.id.toString()] = c
            return questionBanks
        }
        if (id == 1015) {
            var counter = 2
            val questionBanks = linkedMapOf<String, StudyMaterial>()
            for (i in 101501..101510) {
//                Log.d("Xz", "creating "+i+"...")
                val c = StudyMaterial(
                    i.toLong(),
                    "Day " + (i - 101500),
                    "Day " + (i - 101500),
                    "courses/101/title_04_h_0" + (i - 101500) + ".jpg", 0, linkedMapOf(),
                    getQuestions(i), arrayListOf(), arrayListOf(), "", STUDY_MATERIAL_TYPE_QUESTION_BANK, counter++
                )
                questionBanks[c.id.toString()] = c
            }
//            val c = StudyMaterial(
//                101500,
//                "Trial",
//                "Trial",
//                "courses/101/title_04_h_00_trial.jpg", 0, linkedMapOf(),
//                getQuestions(101500), arrayListOf(), getPurchaseInfo(-1), "", STUDY_MATERIAL_TYPE_QUESTION_BANK, 1
//            )
//            questionBanks[c.id.toString()] = c
            return questionBanks
        }
        return linkedMapOf()
    }

    private fun getSubCategories(i: Int): HashMap<String, Category> {
        if (i == 1000) {
            val c1 = Category(1, "Professional Courses", "Professional Courses",
                "images/cat_1.png", getSubCategories(2), getCourses(1))
            return hashMapOf(c1.id.toString() to c1)
        }
        return linkedMapOf()
    }

    private fun getQuestions(i: Int): ArrayList<Question> {
        if(i == 101401) {
            return  arrayListOf(
                Question(
                    1, "For compliance testing which sampling method is more useful ?", "",
                    arrayListOf(QuestionOption(1, "Attribute Sampling", "")),
                    2
                ),
                Question(
                    2, "First step of Risk Assessement is to?", "",
                    arrayListOf(QuestionOption(1, "Identify Assets", "")),
                    2
                ),
                Question(
                    3,
                    "Risk that a misstatement could occur but may not be detected and corrected or prevented by entity’s internal control mechanism is called",
                    "",
                    arrayListOf(QuestionOption(1, "Control Risk", "")),
                    2
                ),
                Question(
                    4,
                    "In any given scenario, compliance testing checks for the presence of controls whereas substantive testing checks the integrity of contents i.e. test of individual transactions. True or False",
                    "",
                    arrayListOf(QuestionOption(1, "TRUE", "")),
                    2
                ),
                Question(
                    5, "Purpose of CSA (Control Self Assessement)", "",
                    arrayListOf(QuestionOption(1, "enhance audit responsibilities", "")),
                    2
                )
            )
        }
        if(i == 101400) {
            return  arrayListOf(
                Question(
                    1, "For compliance testing which sampling method is more useful ?", "",
                        arrayListOf(QuestionOption(1, "Attribute Sampling", ""),
                        QuestionOption(2, "Attribute Sampling", ""),
                        QuestionOption(3, "Attribute Sampling", ""),
                        QuestionOption(4, "Attribute Sampling", "")),
                    2, 0, false, getVideos(1)
                ),
                Question(
                    2, "First step of Risk Assessement is to?", "",
                    arrayListOf(QuestionOption(1, "Attribute Sampling", ""),
                        QuestionOption(2, "Attribute Sampling", ""),
                        QuestionOption(3, "Attribute Sampling", ""),
                        QuestionOption(4, "Attribute Sampling", "")),
                    2
                ),
                Question(
                    3, "For compliance testing which sampling method is more useful ?", "",
                    arrayListOf(QuestionOption(1, "Attribute Sampling", ""),
                        QuestionOption(2, "Attribute Sampling", ""),
                        QuestionOption(3, "Attribute Sampling", ""),
                        QuestionOption(4, "Attribute Sampling", "")),
                    2, 0, false, getVideos(2)
                ),
                Question(
                    4, "For compliance testing which sampling method is more useful ?", "",
                    arrayListOf(QuestionOption(1, "Attribute Sampling", ""),
                        QuestionOption(2, "Attribute Sampling", ""),
                        QuestionOption(3, "Attribute Sampling", ""),
                        QuestionOption(4, "Attribute Sampling", "")),
                    2, 0, false, getVideos(3)
                ),
                Question(
                    5, "For compliance testing which sampling method is more useful ?", "",
                    arrayListOf(QuestionOption(1, "Attribute Sampling", ""),
                        QuestionOption(2, "Attribute Sampling", ""),
                        QuestionOption(3, "Attribute Sampling", ""),
                        QuestionOption(4, "Attribute Sampling", "")),
                    2, 0, false, getVideos(101400)
                )
            )
        }
        if(i == 101200) {
            return  arrayListOf(
                Question(
                    5, "For compliance testing which sampling method is more useful ?", "",
                    arrayListOf(QuestionOption(1, "Attribute Sampling", ""),
                        QuestionOption(2, "Attribute Sampling", ""),
                        QuestionOption(3, "Attribute Sampling", ""),
                        QuestionOption(4, "Attribute Sampling", "")),
                    2, 0, false, getVideos(101400)
                )
            )
        }
        if(i == 1011) {
            return  arrayListOf(
                Question(
                    1, "About Risk Assessement?", "",
                    arrayListOf(
                        QuestionOption(1, "Step 01 - Identify Assets", ""),
                        QuestionOption(
                            2,
                            "Step 02 - Identify Vulnerability / Threats",
                            ""
                        ),
                        QuestionOption(3, "Step 03 - Impact Analysis", ""),
                        QuestionOption(
                            4,
                            "Step 04 - Prioritize on the basis of Impact",
                            ""
                        ),
                        QuestionOption(5, "Step 05 - Evaluate Controls", ""),
                        QuestionOption(6, "Step 06 - Apply Appropriate Controls", "")
                    ),
                    2
                ),
                Question(
                    2, "Types of Risks?", "",
                    arrayListOf(
                        QuestionOption(1, "Inherent Risks - ", ""),
                        QuestionOption(2, "Residual Risks - ", ""),
                        QuestionOption(3, "Detection Risks - ", ""),
                        QuestionOption(4, "Control Risks - ", ""),
                        QuestionOption(5, "Audit Risks - ", "")
                    ),
                    2
                ),
                Question(
                    3, "Compliance & Substantive Testing?", "",
                    arrayListOf(
                        QuestionOption(
                            1,
                            "compliance testing test controls, while substantive testing tests details",
                            ""
                        ),
                        QuestionOption(
                            2,
                            "compliance testing checks for the presence of controls whereas substantive testing checks the integrity of contents",
                            ""
                        ),
                        QuestionOption(
                            3,
                            "In any given scenario, outcome/result of compliance testing will form the basis for planning of substantive testing.  ",
                            ""
                        ),
                        QuestionOption(
                            4,
                            "attribute sampling method (either control is present or absent) will be useful when testing for compliance.",
                            ""
                        )
                    ),
                    2
                )
            )
        }
        if(i == 1012) {
            return  arrayListOf(
                Question(
                    1, "Information System Management", "",
                    arrayListOf(
                        QuestionOption(
                            1,
                            "Information security management is the collection of policies, processes, and procedures that ensure an organization’s security policy is effective. Security management is composed of a number of distinct and interrelated processes, including policy development and enforcement, security awareness training, user access management, security incident management, vulnerability management, service provider management, encryption, network access management, environmental controls, and physical access controls. Ongoing executive support is key to the success of a security management program.",
                            ""
                        ),
                        QuestionOption(
                            2,
                            "Information security management will be effective only when it has an appropriate level of executive-level support. A level of visible commitment to security management is required, including the ratification of security policies, delegation of key roles and responsibilities, allocation of resources, and leadership by example. Without executive support as a foundation, an organization’s information security program cannot hope to succeed and be effective.",
                            ""
                        )
                    ),
                    2
                ),
                Question(
                    2, "Policies & Procedures", "",
                    arrayListOf(
                        QuestionOption(
                            1,
                            "Statement of executive support: The policy document must clearly state that the information security policy has the full and unwavering support of the organization’s executives. The policy should include a signature block that shows their support in writing. ",
                            ""
                        ),
                        QuestionOption(
                            2,
                            "Roles and responsibilities: Information security policy should define security-related roles and responsibilities, including who is responsible for policy development and enforcement. It should also include who is responsible for performing risk assessments and making risk-based decisions. The policy should also describe how the structure of asset ownership works and clearly state how asset owners have some responsibilities in protecting the assets that they control. Finally, the policy should state the responsibilities that all employees have. ",
                            ""
                        ),
                        QuestionOption(
                            3,
                            "Value of information-related assets: The information security policy should include the idea that the organization’s information system and information are valued assets that deserve protection. While the tangibility of some assets may be difficult to value monetarily, they are valuable nonetheless and must be protected. ",
                            ""
                        ),
                        QuestionOption(
                            4,
                            "Protection of information assets: Since the organization’s informationrelated assets have value, they must be protected. The policy should describe the ways that information assets are protected through controls to protect their confidentiality, integrity, and availability. ",
                            ""
                        )
                    ),
                    2
                )
            )
        }
        if(i == 1) {
            return  arrayListOf(
                Question(
                    1,
                    "The decisions and actions of an IS auditor are MOST likely to affect which of the following types of risk?",
                    "",
                    arrayListOf(
                        QuestionOption(1, "Inherent", ""),
                        QuestionOption(
                            2,
                            "Detection",
                            "Detection risk is directly affected by the IS auditor's seelction of audit procedures and techniques. Detecton risk is the risk that a review will not detect or notice a material issue."
                        ),
                        QuestionOption(3, "Control", ""),
                        QuestionOption(4, "Business", "")
                    ),
                    2
                ),
                Question(
                    2,
                    "An IS auditor is evaluating management's risk assessement of information systems. The IS auditor should FIRST review:",
                    "",
                    arrayListOf(
                        QuestionOption(1, "Inherent", ""),
                        QuestionOption(2, "The controls in place", ""),
                        QuestionOption(3, "The effectiveness of the controls", ""),
                        QuestionOption(
                            4,
                            "The mechanism for monitoring the risk",
                            "One of the key factors to be considered while assessing the information systems risks is the value of the systems (the assets) and the threats & vulnerabilities affecting the assets. The risk related to the use of information of assets should be evaluated in isolation from the installed controls."
                        )
                    ),
                    4
                ),
                Question(
                    3,
                    "In the course of performing a risk analysis, an IS auditor has identified threats and potential impacts. Next, the IS auditor should:",
                    "",
                    arrayListOf(
                        QuestionOption(
                            1,
                            "Ensure the risk assessement is aligned to management's risk assessement process",
                            ""
                        ),
                        QuestionOption(
                            2,
                            "Identify information assets and the underlying systems.",
                            ""
                        ),
                        QuestionOption(
                            3,
                            "Disclose the threats and impacts to the management",
                            ""
                        ),
                        QuestionOption(
                            4,
                            "Identify and evaluate the existing controls",
                            "It is important for an IS auditor to identify and evaluate the existence and effectiveness of existing and planned controls so that the risk level can be calculated after the potential threats and possible impacts are identified."
                        )
                    ),
                    4
                ),
                Question(
                    4,
                    "When developing a risk-based audit strategy, an IS auditor should conduct a risk assessement to ensure that ",
                    "",
                    arrayListOf(
                        QuestionOption(
                            1,
                            "Controls needed to mitigate risk are in place",
                            ""
                        ),
                        QuestionOption(
                            2,
                            "Vulnerabilities and threats are identified",
                            "In developing a risk-based audit strategy, it is critical that the risk and vulnerabilities be understood. This will determine the areas to be audited and the extent of coverage"
                        ),
                        QuestionOption(3, "Audit risk is considered", ""),
                        QuestionOption(4, "A gap analysis is appropriate", "")
                    ),
                    2
                ),
                Question(
                    5,
                    "While conducting an audit, an IS auditor detects the presence of Virus. What should be the IS auditor's next step?",
                    "",
                    arrayListOf(
                        QuestionOption(1, "Observe the response mechanism", ""),
                        QuestionOption(2, "Clear the virus from the network", ""),
                        QuestionOption(
                            3,
                            "Inform appropriate personnel immediately",
                            "The first thing an IS auditor should do after detecting the virus is to alert the organization to its presence, then wait for their response"
                        ),
                        QuestionOption(4, "Ensure deletion of the virus", "")
                    ),
                    3
                ),
                Question(
                    6,
                    "When performing a computer forensic investigation, in regard to the evidence gathered, an IS auditor should be MOSt concerned with:",
                    "",
                    arrayListOf(
                        QuestionOption(1, "Analysis", ""),
                        QuestionOption(2, "Evaluation", ""),
                        QuestionOption(
                            3,
                            "Preservation",
                            "Preservation and documentation of evidence for review by law enforcement and judicial authorities are of primary concern when conducting an investigation. Failure to properly preserve the evidence could jeopradize the admissibility of the evidence in legal proceedings"
                        ),
                        QuestionOption(4, "Disclosure", "")
                    ),
                    3
                ),
                Question(
                    7, "The PRIMARY purpose of an IT forensic audit is:", "",
                    arrayListOf(
                        QuestionOption(
                            1,
                            "To participate in investigations related to corporate fraud",
                            ""
                        ),
                        QuestionOption(
                            2,
                            "The systematic collection and analysis of evidence after a system irregularity",
                            "The systematic collection and analysis of evidence after a system irregularity best describes a forensic audit. The evidence collected could ten be analyzed and used in judicial proceedings"
                        ),
                        QuestionOption(
                            3,
                            "To assess the correctness of an organization's financial statements",
                            ""
                        ),
                        QuestionOption(
                            4,
                            "To preserve evidence of criminal activity",
                            ""
                        )
                    ),
                    2
                ),
                Question(
                    8,
                    "An IS auditor reviews one day of logs for a remotely managed server and findss one case where logging failed and the backup restarts cannot be confirmed. What should the IS auditor do?",
                    "",
                    arrayListOf(
                        QuestionOption(1, "Issue an audit finding", ""),
                        QuestionOption(2, "Seek an explanation from IS management", ""),
                        QuestionOption(
                            3,
                            "Review the classifications of data held on the server",
                            ""
                        ),
                        QuestionOption(
                            4,
                            "Expand the sample of logs reviewed",
                            "IS Audit and Assurance Standards require that an IS auditor gather sufficient and appropriate audit evidence. The IS auditor has found a potential problem and now needs to determine whether this is an isolated incident or a systematic control failure."
                        )
                    ),
                    4
                ),
                Question(
                    9, "Sharing risk is a key factor in which of the following methods of managing risk?", "",
                    arrayListOf(
                        QuestionOption(
                            1,
                            "Transferring risk",
                            "Transfering risk (e.g., by taking an insurance policy) is a way to share risk"
                        ),
                        QuestionOption(2, "Tolerating risk", ""),
                        QuestionOption(3, "Terminating risk", ""),
                        QuestionOption(4, "Treating risk", "")
                    ),
                    1
                ),
                Question(
                    10,
                    "Which of the following is the FIRST step performed prior to creating a risk ranking for the annual internal IS audit plan?",
                    "",
                    arrayListOf(
                        QuestionOption(
                            1,
                            "Which of the following is the FIRST step performed prior to creating a risk ranking for the annual internal IS audit plan?",
                            ""
                        ),
                        QuestionOption(
                            2,
                            "Define the audit universe",
                            "In a risk-based audit approach; the IS auditor identifies risk to the organization based on the nature of the business. In order to plan an annual audit cycle, the types of risk must be ranked. To rank the types of risk, the auditor must first define the audit universe by considering the IT strategic plan, organizational structure and authorization matrix."
                        ),
                        QuestionOption(3, "Identify the critical controls", ""),
                        QuestionOption(4, "Determine the testing approach", "")
                    ),
                    2
                ),
                Question(
                    11,
                    "An IS auditor is reviewing a project risk assessement and notices that the overall risk level is high due to confidentiality requirements. Which of the following types of risk is normally high due to the number of users and business areas the project may affect?",
                    "",
                    arrayListOf(
                        QuestionOption(1, "Control risk", ""),
                        QuestionOption(2, "Compliance risk", ""),
                        QuestionOption(
                            3,
                            "Inherent risk",
                            "Inherent risk is normally high due to the number of users and business areas that may be affected. Inherent risk is the risk level or exposures without taking into account the actions that the management has taken or might take."
                        ),
                        QuestionOption(4, "Residual risk", "")
                    ),
                    3
                ),
                Question(
                    12,
                    "An IS auditor performing an audit of the risk assessement process should FIRST confirm that:",
                    "",
                    arrayListOf(
                        QuestionOption(
                            1,
                            "Reasonable threats to the information assets are identified",
                            ""
                        ),
                        QuestionOption(
                            2,
                            "Technical and organization vulnerabilities have been analyzed",
                            ""
                        ),
                        QuestionOption(
                            3,
                            "Assets have been identified and ranked",
                            "Identification and ranking of information assets (e.g., data criticality, sensitivity, location of assets) will set the tone or scope of how to assess risk in relation to the orgaizational value of the asset."
                        ),
                        QuestionOption(
                            4,
                            "The effects of potential security breaches has been evaluated",
                            ""
                        )
                    ),
                    3
                )
            )
        }
        return arrayListOf()
    }

    fun addDebugObject(dataSource: DataSource, id: String, value: Any) {
        val dbRef = dataSource.getDatabase().getReference("debug")
        dbRef.child(id).setValue(value)
    }

    fun copyQuestionBank(dataSource: DataSource) {
        val ref1 = "debug/studyMaterials/101200"
        val target1 = "cats/v2/cats/1/courses/101/sections/1015/studyMaterials/101511"
        var reference1 =
            dataSource.getDatabase().getReference(ref1)
        reference1.keepSynced(true)
        reference1.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val questionBank = snapshot.getValue(StudyMaterial::class.java)
                if (questionBank != null) {
                    dataSource.getDatabase().getReference(target1).setValue(questionBank)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) { }
        })

//        val ref2 = "debug/studyMaterials/101201"
//        val target2 = "cats/v2/cats/1/courses/101/sections/1015/studyMaterials/101512"
//        var reference2 =
//                dataSource.getDatabase().getReference(ref2)
//        reference2.keepSynced(true)
//        reference2.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val questionBank = snapshot.getValue(StudyMaterial::class.java)
//                if (questionBank != null) {
//                    dataSource.getDatabase().getReference(target2).setValue(questionBank)
//                }
//            }
//            override fun onCancelled(databaseError: DatabaseError) { }
//        })
//
//        val ref3 = "debug/studyMaterials/101202"
//        val target3 = "cats/v2/cats/1/courses/101/sections/1015/studyMaterials/101513"
//        var reference3 =
//                dataSource.getDatabase().getReference(ref3)
//        reference3.keepSynced(true)
//        reference3.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val questionBank = snapshot.getValue(StudyMaterial::class.java)
//                if (questionBank != null) {
//                    dataSource.getDatabase().getReference(target3).setValue(questionBank)
//                }
//            }
//            override fun onCancelled(databaseError: DatabaseError) { }
//        })
//
//        val ref4 = "debug/studyMaterials/101203"
//        val target4 = "cats/v2/cats/1/courses/101/sections/1015/studyMaterials/101514"
//        var reference4 =
//            dataSource.getDatabase().getReference(ref4)
//        reference4.keepSynced(true)
//        reference4.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val questionBank = snapshot.getValue(StudyMaterial::class.java)
//                if (questionBank != null) {
//                    dataSource.getDatabase().getReference(target4).setValue(questionBank)
//                }
//            }
//            override fun onCancelled(databaseError: DatabaseError) { }
//        })

//        val ref5 = "debug/studyMaterials/101204"
//        val target5 = "cats/v2/cats/1/courses/101/sections/1013/studyMaterials/101301"
//        var reference5 =
//            dataSource.getDatabase().getReference(ref5)
//        reference5.keepSynced(true)
//        reference5.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val questionBank = snapshot.getValue(StudyMaterial::class.java)
//                if (questionBank != null) {
//                    dataSource.getDatabase().getReference(target5).setValue(questionBank)
//                }
//            }
//            override fun onCancelled(databaseError: DatabaseError) { }
//        })
    }

    fun uploadImages(activity: SimpleActivity, dataSource: DataSource) {
        val files = File(activity.getInternalStoragePath(), "Xzaminer")
        files.listFiles()?.forEach {
            val riversRef = dataSource.getStorage().getReference("courses/101").child(it.name)
            riversRef.putFile(Uri.fromFile(it))
        }
    }
}
