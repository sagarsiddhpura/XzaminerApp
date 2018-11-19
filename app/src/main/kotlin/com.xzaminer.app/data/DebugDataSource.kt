package com.xzaminer.app.data

import com.xzaminer.app.category.Category
import com.xzaminer.app.quiz.Question
import com.xzaminer.app.quiz.QuestionBank
import com.xzaminer.app.quiz.QuestionOption


class DebugDataSource {
    fun initMockDataRealtimeDatabase(dataSource: DataSource) {
        val dbRef = dataSource.getCatsDatabase()

        val c1 = Category(1, "Professional Courses", "Professional Courses",
            "images/cat_1.png", getSubCategories(1), getQuestionBanks(1))
        dbRef.child("cats").setValue(hashMapOf(c1.id.toString() to c1))
        dbRef.push()
    }

    private fun getSubCategories(i: Int): HashMap<String, Category> {
        if (i == 1) {
            val c1 = Category(101, "CISA", "CISA",
                "images/cat_2.png", getSubCategories(101), getQuestionBanks(101))
            return hashMapOf(c1.id.toString() to c1)
        }
        return hashMapOf()
    }

    private fun getQuestionBanks(i: Int): HashMap<String, QuestionBank> {
        return hashMapOf()
    }

    private fun getQuestions(i: Int): ArrayList<Question> {
        return  arrayListOf(
                    Question(1, "The decisions and actions of an IS auditor are MOST likely to affect which of the following types of risk?", "",
                        arrayListOf(QuestionOption(1, "Inherent", ""),
                            QuestionOption(2, "Detection", "Detection risk is directly affected by the IS auditor's seelction of audit procedures and techniques. Detecton risk is the risk that a review will not detect or notice a material issue."),
                            QuestionOption(3, "Control", ""),
                            QuestionOption(4, "Business", "")),
                        2),
                    Question(2, "An IS auditor is evaluating management's risk assessement of information systems. The IS auditor should FIRST review:", "",
                        arrayListOf(QuestionOption(1, "Inherent", ""),
                            QuestionOption(2, "The controls in place", ""),
                            QuestionOption(3, "The effectiveness of the controls", ""),
                            QuestionOption(4, "The mechanism for monitoring the risk", "One of the key factors to be considered while assessing the information systems risks is the value of the systems (the assets) and the threats & vulnerabilities affecting the assets. The risk related to the use of information of assets should be evaluated in isolation from the installed controls.")),
                        4),
                    Question(3, "In the course of performing a risk analysis, an IS auditor has identified threats and potential impacts. Next, the IS auditor should:", "",
                        arrayListOf(QuestionOption(1, "Ensure the risk assessement is aligned to management's risk assessement process", ""),
                            QuestionOption(2, "Identify information assets and the underlying systems.", ""),
                            QuestionOption(3, "Disclose the threats and impacts to the management", ""),
                            QuestionOption(4, "Identify and evaluate the existing controls", "It is important for an IS auditor to identify and evaluate the existence and effectiveness of existing and planned controls so that the risk level can be calculated after the potential threats and possible impacts are identified.")),
                        4),
                    Question(4, "When developing a risk-based audit strategy, an IS auditor should conduct a risk assessement to ensure that ", "",
                        arrayListOf(QuestionOption(1, "Controls needed to mitigate risk are in place", ""),
                            QuestionOption(2, "Vulnerabilities and threats are identified", "In developing a risk-based audit strategy, it is critical that the risk and vulnerabilities be understood. This will determine the areas to be audited and the extent of coverage"),
                            QuestionOption(3, "Audit risk is considered", ""),
                            QuestionOption(4, "A gap analysis is appropriate", "")),
                        2),
                    Question(5, "While conducting an audit, an IS auditor detects the presence of Virus. What should be the IS auditor's next step?", "",
                        arrayListOf(QuestionOption(1, "Observe the response mechanism", ""),
                            QuestionOption(2, "Clear the virus from the network", ""),
                            QuestionOption(3, "Inform appropriate personnel immediately", "The first thing an IS auditor should do after detecting the virus is to alert the organization to its presence, then wait for their response"),
                            QuestionOption(4, "Ensure deletion of the virus", "")),
                        3),
                    Question(6, "When performing a computer forensic investigation, in regard to the evidence gathered, an IS auditor should be MOSt concerned with:", "",
                        arrayListOf(QuestionOption(1, "Analysis", ""),
                            QuestionOption(2, "Evaluation", ""),
                            QuestionOption(3, "Preservation", "Preservation and documentation of evidence for review by law enforcement and judicial authorities are of primary concern when conducting an investigation. Failure to properly preserve the evidence could jeopradize the admissibility of the evidence in legal proceedings"),
                            QuestionOption(4, "Disclosure", "")),
                        3),
                    Question(7, "The PRIMARY purpose of an IT forensic audit is:", "",
                        arrayListOf(QuestionOption(1, "To participate in investigations related to corporate fraud", ""),
                            QuestionOption(2, "The systematic collection and analysis of evidence after a system irregularity", "The systematic collection and analysis of evidence after a system irregularity best describes a forensic audit. The evidence collected could ten be analyzed and used in judicial proceedings"),
                            QuestionOption(3, "To assess the correctness of an organization's financial statements", ""),
                            QuestionOption(4, "To preserve evidence of criminal activity", "")),
                        2),
                    Question(8, "An IS auditor reviews one day of logs for a remotely managed server and findss one case where logging failed and the backup restarts cannot be confirmed. What should the IS auditor do?", "",
                        arrayListOf(QuestionOption(1, "Issue an audit finding", ""),
                            QuestionOption(2, "Seek an explanation from IS management", ""),
                            QuestionOption(3, "Review the classifications of data held on the server", ""),
                            QuestionOption(4, "Expand the sample of logs reviewed", "IS Audit and Assurance Standards require that an IS auditor gather sufficient and appropriate audit evidence. The IS auditor has found a potential problem and now needs to determine whether this is an isolated incident or a systematic control failure.")),
                        4),
                    Question(9, "Sharing risk is a key factor in which of the following methods of managing risk?", "",
                        arrayListOf(QuestionOption(1, "Transferring risk", "Transfering risk (e.g., by taking an insurance policy) is a way to share risk"),
                            QuestionOption(2, "Tolerating risk", ""),
                            QuestionOption(3, "Terminating risk", ""),
                            QuestionOption(4, "Treating risk", "")),
                        1),
                    Question(10, "Which of the following is the FIRST step performed prior to creating a risk ranking for the annual internal IS audit plan?", "",
                        arrayListOf(QuestionOption(1, "Which of the following is the FIRST step performed prior to creating a risk ranking for the annual internal IS audit plan?", ""),
                            QuestionOption(2, "Define the audit universe", "In a risk-based audit approach; the IS auditor identifies risk to the organization based on the nature of the business. In order to plan an annual audit cycle, the types of risk must be ranked. To rank the types of risk, the auditor must first define the audit universe by considering the IT strategic plan, organizational structure and authorization matrix."),
                            QuestionOption(3, "Identify the critical controls", ""),
                            QuestionOption(4, "Determine the testing approach", "")),
                        2),
                    Question(11, "An IS auditor is reviewing a project risk assessement and notices that the overall risk level is high due to confidentiality requirements. Which of the following types of risk is normally high due to the number of users and business areas the project may affect?", "",
                        arrayListOf(QuestionOption(1, "Control risk", ""),
                            QuestionOption(2, "Compliance risk", ""),
                            QuestionOption(3, "Inherent risk", "Inherent risk is normally high due to the number of users and business areas that may be affected. Inherent risk is the risk level or exposures without taking into account the actions that the management has taken or might take."),
                            QuestionOption(4, "Residual risk", "")),
                        3),
                    Question(12, "An IS auditor performing an audit of the risk assessement process should FIRST confirm that:", "",
                        arrayListOf(QuestionOption(1, "Reasonable threats to the information assets are identified", ""),
                            QuestionOption(2, "Technical and organization vulnerabilities have been analyzed", ""),
                            QuestionOption(3, "Assets have been identified and ranked", "Identification and ranking of information assets (e.g., data criticality, sensitivity, location of assets) will set the tone or scope of how to assess risk in relation to the orgaizational value of the asset."),
                            QuestionOption(4, "The effects of potential security breaches has been evaluated", "")),
                        3)
                   )
    }

    fun addDebugObject(dataSource: DataSource, id: String, value: Any) {
        val dbRef = dataSource.getDatabase().getReference("debug")
        dbRef.child(id).setValue(value)
    }
}
