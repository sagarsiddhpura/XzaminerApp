package com.xzaminer.app.data

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.xzaminer.app.category.Category
import com.xzaminer.app.course.Course
import com.xzaminer.app.course.CourseSection
import com.xzaminer.app.studymaterial.Question
import com.xzaminer.app.studymaterial.QuestionOption
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.utils.STUDY_MATERIAL_TYPE_QUESTION_BANK
import com.xzaminer.app.utils.STUDY_MATERIAL_TYPE_STUDY_MATERIAL


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
                "CISA (Certified Information System Auditor)",
                "Congratulations on choosing to become a Certified Information Systems Auditor (CISA). Whether you have worked for several years in the field of information systems auditing or have just recently been introduced to the world of controls, assurance, and security, don’t underestimate the hard work and dedication required to obtain and maintain CISA certification. Although ambition and motivation are required, the rewards can far exceed the effort.\n" +
                        "You probably never imagined you would find yourself working in the world of auditing or looking to obtain a professional auditing certification. Perhaps the increase in legislative or regulatory requirements for information system security led to your introduction to this field. ",
                "images/cat_2.png",
                getSections(101), arrayListOf()
            )
            return linkedMapOf(c1.id.toString() to c1)
        }
        return linkedMapOf()
    }

    private fun getSections(id: Int): HashMap<String, CourseSection> {
        if (id == 101) {
            val s1 = CourseSection(
                1011,
                "30 Days Course (Concepts)",
                "30 Days Course (Concepts)",
                "", getConcepts(1011), STUDY_MATERIAL_TYPE_STUDY_MATERIAL,
                arrayListOf()
            )
            val s2 = CourseSection(
                1012,
                "30 Days Course (Questions)",
                "30 Days Course (Questions)",
                "", getQuestionBanks(1012), STUDY_MATERIAL_TYPE_QUESTION_BANK,
                arrayListOf()
            )
            val s3 = CourseSection(
                1013,
                "Review Manuals",
                "Review Manuals",
                "", getReviewManuals(1013), STUDY_MATERIAL_TYPE_STUDY_MATERIAL,
                arrayListOf()
            )
            val s4 = CourseSection(
                1014,
                "Flash Cards",
                "Flash Cards",
                "", getFlashCards(1014), STUDY_MATERIAL_TYPE_STUDY_MATERIAL,
                arrayListOf()
            )
            val sections = linkedMapOf(s1.id.toString() to s1, s2.id.toString() to s2, s3.id.toString() to s3, s4.id.toString() to s4)
            return sections
        }
        return hashMapOf()
    }

    private fun getFlashCards(id: Int): LinkedHashMap<String, StudyMaterial> {
        if (id == 1014) {
            val c1 = StudyMaterial(
                101400,
                "Domain 01",
                "Domain 01",
                "images/im_flash_cards.png", 0, linkedMapOf(),
                getQuestions(1013), arrayListOf(), ""
            )
            val flashCards = linkedMapOf(c1.id.toString() to c1)
            for (i in 101401..101404) {
                val c = StudyMaterial(
                    i.toLong(),
                    "Domain " + (i - 101300),
                    "Domain " + (i - 101300),
                    "images/im_flash_card.png", 0, linkedMapOf(),
                    getQuestions(i), arrayListOf(), ""
                )
                flashCards[c.id.toString()] = c
            }
            return flashCards
        }
        return linkedMapOf()
    }

    private fun getReviewManuals(id: Int): LinkedHashMap<String, StudyMaterial> {
        if (id == 1013) {
            val c1 = StudyMaterial(
                101300,
                "Domain 04",
                "Domain 04",
                "images/im_review_manual.png", 0, linkedMapOf(),
                getQuestions(1013), arrayListOf(), ""
            )
            val reviewManuals = linkedMapOf(c1.id.toString() to c1)
            for (i in 101301..101304) {
                val c = StudyMaterial(
                    i.toLong(),
                    "Domain " + (i - 101300),
                    "Domain " + (i - 101300),
                    "images/im_review_manual.png", 0, linkedMapOf(),
                    getQuestions(i), arrayListOf(), ""
                )
                reviewManuals[c.id.toString()] = c
            }
            return reviewManuals
        }
        return linkedMapOf()
    }

    private fun getConcepts(id: Int): LinkedHashMap<String, StudyMaterial> {
        if (id == 1011) {
            val c1 = StudyMaterial(
                101100,
                "CISA 30 Days Course (Concepts)",
                "CISA 30 Days Course (Concepts)",
                "images/cat_303.png", 0, linkedMapOf(),
                getQuestions(1011), arrayListOf(), ""
            )
            val studyMats = linkedMapOf(c1.id.toString() to c1)
            for (i in 101101..101130) {
                val c = StudyMaterial(
                    i.toLong(),
                    "Day " + (i - 101100),
                    "Day " + (i - 101100),
                    "images/im_concept.png", 0, linkedMapOf(),
                    getQuestions(i), arrayListOf(), ""
                )
                studyMats[c.id.toString()] = c
            }
            return studyMats
        }
        return linkedMapOf()
    }

    private fun getQuestionBanks(id: Int): LinkedHashMap<String, StudyMaterial> {
        if (id == 1012) {
            val questionBanks = linkedMapOf<String, StudyMaterial>()
            for (i in 101203..101232) {
                Log.d("Xz", "creating "+i+"...")
                val c = StudyMaterial(
                    i.toLong(),
                    "Day " + (i - 101200),
                    "Day " + (i - 101200),
                    "images/im_question_bank.png", 0, linkedMapOf(),
                    getQuestions(i), arrayListOf(), "", STUDY_MATERIAL_TYPE_QUESTION_BANK
                )
                questionBanks[c.id.toString()] = c
            }
            return questionBanks
        }
        return linkedMapOf()
    }

    private fun getSubCategories(i: Int): HashMap<String, Category> {
        return linkedMapOf()
    }

    private fun getQuestions(i: Int): ArrayList<Question> {
        if(i == 1013) {
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
        val ref1 = "cats/v2/cats/1/subCategories/101/questionBanks/1542708404073"
        val target1 = "cats/v1/cats/1/courses/101/sections/1012/studyMaterials/101200"
        var reference =
            dataSource.getDatabase().getReference(ref1)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val questionBank = snapshot.getValue(StudyMaterial::class.java)
                if (questionBank != null) {
                    dataSource.getDatabase().getReference(target1).setValue(questionBank)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) { }
        })

        val ref2 = "cats/v2/cats/1/subCategories/101/questionBanks/1542708433953"
        val target2 = "cats/v1/cats/1/courses/101/sections/1012/studyMaterials/101201"
        reference =
                dataSource.getDatabase().getReference(ref2)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val questionBank = snapshot.getValue(StudyMaterial::class.java)
                if (questionBank != null) {
                    dataSource.getDatabase().getReference(target2).setValue(questionBank)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) { }
        })

        val ref3 = "cats/v2/cats/1/subCategories/101/questionBanks/1542708444373"
        val target3 = "cats/v1/cats/1/courses/101/sections/1012/studyMaterials/101202"
        reference =
                dataSource.getDatabase().getReference(ref3)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val questionBank = snapshot.getValue(StudyMaterial::class.java)
                if (questionBank != null) {
                    dataSource.getDatabase().getReference(target3).setValue(questionBank)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) { }
        })
    }
}
