package com.xzaminer.app.data

import com.xzaminer.app.category.Category
import com.xzaminer.app.quiz.QuestionBank


class DebugDataSource {
    fun initMockDataRealtimeDatabase(dataSource: DataSource) {
        val dbRef = dataSource.getCatsDatabase()

        val c1 = Category(1, "Students", "School/College Students",
            "images/cat_1.png", getSubCategories(1), getQuestionBanks(1))
        val c2 = Category(2, "IT Professionals", "IT Professionals",
            "images/cat_2.png", getSubCategories(2), getQuestionBanks(2))
        val c3 = Category(3, "Government", "Government",
            "images/cat_3.png", getSubCategories(3), getQuestionBanks(3))
        val c4 = Category(4, "Banks", "Banks",
            "images/cat_4.png", getSubCategories(4), getQuestionBanks(4))
        dbRef.child("cats").setValue(hashMapOf(c1.id.toString() to c1, c2.id.toString() to c2, c3.id.toString() to c3, c4.id.toString() to c4))
        dbRef.push()
    }

    private fun getSubCategories(i: Int): HashMap<String, Category> {
        if (i == 1) {
            val c1 = Category(101, "10th Standard", "10th Standard",
                "images/cat_101.png", getSubCategories(101), getQuestionBanks(101))
            val c2 = Category(102, "12th Standard", "12th Standard",
                "images/cat_102.png", getSubCategories(102), getQuestionBanks(102))
            val c3 = Category(103, "Third Year B. Com", "Third Year B. Com",
                "images/cat_103.png", getSubCategories(103), getQuestionBanks(103))
            val c4 = Category(104, "Third Year B. Arts", "Third Year B. Arts",
                "images/cat_104.png", getSubCategories(104), getQuestionBanks(104))
            val c5 = Category(105, "Third Year B. Science", "Third Year B. Science",
                "images/cat_105.png", getSubCategories(105), getQuestionBanks(105))
            return hashMapOf(c1.id.toString() to c1, c2.id.toString() to c2, c3.id.toString() to c3, c4.id.toString() to c4, c5.id.toString() to c5)
        }
        if (i == 2) {
            return hashMapOf()
        }
        if (i == 3) {
            val c1 = Category(301, "IAS", "Indian Administrative Services",
                "images/cat_301.png", getSubCategories(301), getQuestionBanks(301))
            val c2 = Category(302, "IRS", "Indian Revenue Services",
                "images/cat_302.png", getSubCategories(302), getQuestionBanks(302))
            val c3 = Category(303, "UPSC", "UPSC",
                "images/cat_303.png", getSubCategories(303), getQuestionBanks(303))
            return hashMapOf(c1.id.toString() to c1, c2.id.toString() to c2, c3.id.toString() to c3)
        }
        if (i == 4) {
            return hashMapOf()
        }
        if (i == 101) {
            val c1 = Category(1011, "SSC - Maharashtra Board", "SSC - Maharashtra Board",
                "images/cat_1011.png", getSubCategories(1011), getQuestionBanks(1011))
            val c2 = Category(1012, "SSC - Kerela Board", "SSC - Kerela Board",
                "images/cat_1012.png", getSubCategories(1012), getQuestionBanks(1012))
            val c3 = Category(1013, "CBSE - Delhi Board", "CBSE - Delhi Board",
                "images/cat_1013.png", getSubCategories(1013), getQuestionBanks(1013))
            return hashMapOf(c1.id.toString() to c1, c2.id.toString() to c2, c3.id.toString() to c3)
        }
        return hashMapOf()
    }

    private fun getQuestionBanks(i: Int): HashMap<String, QuestionBank> {
//        if (i == 1011) {
//            return hashMapOf()
//        }
//        if (i == 102) {
//            val a1 = QuestionBank(1021, "Shree Ganesha Stuti", "Shree Ganesha Stuti", "05:45", "", 0,
//                hashMapOf(GENRE to arrayListOf("Devotional")), arrayListOf())
//            return hashMapOf(a1.id.toString() to a1)
//        }
//        if (i == 401) {
//            val a1 = QuestionBank(4011, "Inspirational Volume 1", "Inspirational Volume 1", "04:23", "", 0,
//                hashMapOf(GENRE to arrayListOf("Inspirational, Uplifting")), arrayListOf())
//            val a2 = QuestionBank(4012, "Inspirational Volume 2", "Inspirational Volume 2", "06:13", "", 0,
//                hashMapOf(GENRE to arrayListOf("Inspirational, Uplifting")), arrayListOf())
//            return hashMapOf(a1.id.toString() to a1, a2.id.toString() to a2)
//        }
//        if (i == 402) {
//            return hashMapOf()
//        }
//        if (i == 1) {
//            val a1 = QuestionBank(1001, "History of Maharashtra", "History of Maharashtra", "07:00", "", 0,
//                hashMapOf(GENRE to arrayListOf("Historical")), arrayListOf())
//            return hashMapOf(a1.id.toString() to a1)
//        }
//        if (i == 4) {
//            val a1 = QuestionBank(4001, "Making of I - Sub", "Making of I is an enlightening journey to discover self. Get immersed in the vast " +
//                    "knowledge tome and be prepared to dive deep into yourself as you would have never have done before.\n\nAs a famous person once said" +
//                    "\"Best place to discover is you\", come for a journey that is unique and stay here for lifetime. This audiobook is available under subscription and purchase",
//                "12:34", "", 0,
//                hashMapOf(GENRE to arrayListOf("Historical"), YEAR to arrayListOf("2009"), PRICE to arrayListOf("Rs. 199"),
//                    PUBLISHER to arrayListOf("Pravin Niar"), PUBLISHED_ON to arrayListOf("Dec 7, 2009"), PUBLISHED_ON to arrayListOf("Dec 7, 2009"),
//                    AGE to arrayListOf("All Ages"), PLAY_COUNT to arrayListOf("316"), ARTIST to arrayListOf("Pravin Niar")),
//                arrayListOf(
//                    Question(1, "Intro", "4001_1", "Intro", "05:57",
//                    "https://firebasestorage.googleapis.com/v0/b/audibooapp.appspot.com/o/audio%2F4001_1.mp3?alt=media&token=", null),
//                    Question(2, "Chikkies", "4001_2", "Chikkies", "06:29",
//                        "https://firebasestorage.googleapis.com/v0/b/audibooapp.appspot.com/o/audio%2F4001_2.mp3?alt=media&token=", null),
//                    Question(3, "Ego", "4001_3", "Ego", "02:48",
//                        "https://firebasestorage.googleapis.com/v0/b/audibooapp.appspot.com/o/audio%2F4001_3.mp3?alt=media&token=", null),
//                    Question(4, "Author", "4001_4", "Author", "01:17",
//                        "https://firebasestorage.googleapis.com/v0/b/audibooapp.appspot.com/o/audio%2F4001_4.mp3?alt=media&token=", null)),
//                arrayListOf(
//                    Purchase(IAP_SUB_YEARLY, getProductName(IAP_SUB_YEARLY), PURCHASE_TYPE_SUBSCRIPTION, null, null, null),
//                    Purchase(IAP_SUB_MONTHLY, getProductName(IAP_SUB_MONTHLY), PURCHASE_TYPE_SUBSCRIPTION, null, null, null),
//                    Purchase("com.audiboo.android.iap.audiobook.4001", getProductName("com.audiboo.android.iap.audiobook.4001"), PURCHASE_TYPE_IAP, null, null, null)))
//            val a2 = QuestionBank(4002, "Making of I - Purchase", "Making of I is an enlightening journey to discover self. Get immersed in the vast " +
//                    "knowledge tome and be prepared to dive deep into yourself as you would have never have done before.\n\nAs a famous person once said" +
//                    "\"Best place to discover is you\", come for a journey that is unique and stay here for lifetime. This audiobook is available under purchase only",
//                "12:34", "", 0,
//                hashMapOf(GENRE to arrayListOf("Historical"), YEAR to arrayListOf("2009"), PRICE to arrayListOf("Rs. 199"),
//                    PUBLISHER to arrayListOf("Pravin Niar"), PUBLISHED_ON to arrayListOf("Dec 7, 2009"), PUBLISHED_ON to arrayListOf("Dec 7, 2009"),
//                    AGE to arrayListOf("All Ages"), PLAY_COUNT to arrayListOf("316"), ARTIST to arrayListOf("Pravin Niar")),
//                arrayListOf(Question(1, "Intro", "4001_1", "Intro", "05:57",
//                    "https://firebasestorage.googleapis.com/v0/b/audibooapp.appspot.com/o/audio%2F4001_1.mp3?alt=media&token=", null),
//                    Question(2, "Chikkies", "4001_2", "Chikkies", "06:29",
//                        "https://firebasestorage.googleapis.com/v0/b/audibooapp.appspot.com/o/audio%2F4001_2.mp3?alt=media&token=", null),
//                    Question(3, "Ego", "4001_3", "Ego", "02:48",
//                        "https://firebasestorage.googleapis.com/v0/b/audibooapp.appspot.com/o/audio%2F4001_3.mp3?alt=media&token=", null),
//                    Question(4, "Author", "4001_5", "Author", "01:17",
//                        "https://firebasestorage.googleapis.com/v0/b/audibooapp.appspot.com/o/audio%2F4001_5.mp3?alt=media&token=", null)),
//                arrayListOf(Purchase(IAP_SUB_YEARLY, getProductName(IAP_SUB_YEARLY), PURCHASE_TYPE_SUBSCRIPTION, null, null, null),
//                    Purchase("com.audiboo.android.iap.audiobook.4002", getProductName("com.audiboo.android.iap.audiobook.4002"), PURCHASE_TYPE_IAP, null, null, null)))
//            return hashMapOf(a1.id.toString() to a1, a2.id.toString() to a2)
//        }
        return hashMapOf()
    }

    fun addDebugObject(dataSource: DataSource, id: String, value: Any) {
        val dbRef = dataSource.getDatabase().getReference("debug")
        dbRef.child(id).setValue(value)
    }
}
