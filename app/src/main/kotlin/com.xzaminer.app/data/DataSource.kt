package com.xzaminer.app.data

import android.app.Activity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.xzaminer.app.billing.PurchaseLog
import com.xzaminer.app.category.Category
import com.xzaminer.app.course.Course
import com.xzaminer.app.course.CourseSection
import com.xzaminer.app.extensions.config
import com.xzaminer.app.studymaterial.StudyMaterial
import com.xzaminer.app.user.User
import java.util.*
import kotlin.collections.ArrayList


private var db: FirebaseDatabase? = null
private var storage: FirebaseStorage? = null

//private var catsDbVersion = "VR1"
//private var userDbVersion = "VR1"
//private var purchaseLogDbVersion = "VR1"

//private var catsDbVersion = "VT1"
//private var userDbVersion = "VT1"
//private var purchaseLogDbVersion = "VT1"

private var catsDbVersion = "VD1"
private var userDbVersion = "VD1"
private var purchaseLogDbVersion = "VD1"

class DataSource {

    private var cats: ArrayList<Category>? = null

    fun getLoggedInUser(activity: Activity): User? = activity.config.getLoggedInUser()

    fun getDatabase(): FirebaseDatabase {
        if (db == null) {
            db = FirebaseDatabase.getInstance()
            (db as FirebaseDatabase).setPersistenceEnabled(true)
        }

        return db!!
    }

    private fun getUsersDatabase(): DatabaseReference {
        return getDatabase().getReference("users/$userDbVersion")
    }

    fun getStorage(): FirebaseStorage {
        if (storage == null) {
            storage = FirebaseStorage.getInstance()
        }

        return storage as FirebaseStorage
    }

    fun getCatsDatabase(): DatabaseReference {
        return getDatabase().getReference("cats/$catsDbVersion")
    }

    fun getChildCategories(catId: Long?, callback: (cats: ArrayList<Category>, name: String) -> Unit) {
        if (catId != null) {
            getCategoryById(catId) {
                val cats: ArrayList<Category> = arrayListOf()
                if (it?.subCategories != null) {
                    cats.addAll(ArrayList(it.subCategories!!.values))
                }

                if (it?.courses != null) {
                    val audToCat: ArrayList<Category> = ArrayList()
                    if (it.courses != null) {
                        it.courses!!.values.forEach {
                            if (it != null) {
                                audToCat.add(Category(it.id, it.name, it.desc, it.image, null, null, true, it.order, it.isVisible))
                            }
                        }
                        cats.addAll(audToCat)
                    }
                }
                callback(cats, it?.name ?: "Xzaminer")
            }
        } else {
            getCats {
                callback(it, "Xzaminer")
            }
        }
    }

    private fun getCats(callback: (cats: ArrayList<Category>) -> Unit) {
//        Log.d("Xz_", "getCats:"+System.nanoTime())
        if (cats != null) {
            callback(cats!!)
        } else {
            val database = getCatsDatabase()
            val dept = database.child("cats")
            dept.keepSynced(true)

            dept.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("Xz_", "getCats listener:"+System.nanoTime())
                    val genericTypeIndicator = object : GenericTypeIndicator<ArrayList<Category>>() {}
                    var categories = snapshot.getValue(genericTypeIndicator)
                    if (categories != null) {
                        categories = categories.filter { it != null } as ArrayList<Category>
                        cats = categories
                        callback(categories)
                    }

//                    Log.d("Xz_", "getCats listener:"+System.nanoTime())
//                    val listCats = arrayListOf<Category>()
//                    for (child in snapshot.getChildren()) {
//                        listCats.add(child.getValue(Category::class.java)!!)
//                        for(innerchild in child.children) {
//                            val e1 = innerchild
//                        }
//                    }
////                    val genericTypeIndicator = object : GenericTypeIndicator<HashMap<Long, Category>>() {}
////                    var categories = snapshot.getValue(CategoryParent::class.java))
//                    if (listCats != null) {
//                        cats = listCats
//                        callback(listCats)
//                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("The read failed: " + databaseError.code)
                }
            })

            dept.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val genericTypeIndicator = object : GenericTypeIndicator<ArrayList<Category>>() {}
                    var categories = snapshot.getValue(genericTypeIndicator)
                    if (categories != null) {
                        categories = (categories as ArrayList<Category>).filter { it != null } as ArrayList<Category>
                        cats = categories
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("The read failed: " + (databaseError?.code ?: ""))
                }
            })
        }
    }

    fun getCategoryById(catId: Long, callback: (cat: Category?) -> Unit) {
        getCats { it ->
            Log.d("Xz_", "getCategoryById:"+System.nanoTime())
            callback(searchCategoryById(catId, it))
        }
    }

    private fun searchCategoryById(catId: Long, localCategories: ArrayList<Category>): Category? {
        // search across categories first
        localCategories.forEach { cat ->
            if (cat.id == catId) {
                return cat
            }
        }
        // not found. Search one level deeper
        localCategories.forEach {
            if (it.subCategories != null) {
                val searchCategoryById = searchCategoryById(catId, ArrayList(it.subCategories!!.values))
                if (searchCategoryById != null) {
                    return searchCategoryById
                }
            }
        }
        return null
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    fun addUser(loggedInUser: User) {
        val dbRef = getUsersDatabase()
        dbRef.child("users").child(loggedInUser.getId()).setValue(loggedInUser)
    }

    fun getUser(userId: String, callback: (user: User?) -> Unit) {
        val database = getUsersDatabase()
        val userRef = database.child("users").child(userId)
        userRef.keepSynced(true)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                callback(user)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("The read failed: " + (databaseError.code ?: ""))
                callback(null)
            }
        })
    }

    fun getQuestionBank(qbId: Long?, callback: (questionBank: StudyMaterial?) -> Unit) {
        if (qbId != null) {
            getCats { it ->
                callback(searchQuestionBankById(qbId, it))
            }
        }
    }

    private fun searchQuestionBankById(questionBankId: Long, localCategories: ArrayList<Category>): StudyMaterial? {
        // search across categories first
        localCategories.forEach { cat ->
            if (cat.courses != null) {
                cat.courses!!.values.forEach { course ->
                    course?.sections.values.forEach { section ->
                        if (section != null && section.studyMaterials[questionBankId.toString()] != null) {
                            return section.studyMaterials[questionBankId.toString()]
                        }
                    }
                }
            }
        }
        // not found. Search one level deeper
        localCategories.forEach {
            if (it.subCategories != null) {
                val questionBank = searchQuestionBankById(questionBankId, ArrayList(it.subCategories!!.values))
                if (questionBank != null) {
                    return questionBank
                }
            }
        }
        return null
    }

    fun getQuestionBankFromUser(userId: String, quizId: Long?, callback: (quiz: StudyMaterial?) -> Unit) {
        getUser(userId) {
            val quiz = it?.quizzes?.values?.find { it != null && it.id == quizId }
            callback(quiz)
        }
    }

    fun getCategoryPath(catId: Long, callback: (path: String?) -> Unit): Unit {
        getCats { it ->
            callback(getCategoryPathById(catId, it, ""))
        }
    }

    private fun getCategoryPathById(catId: Long, localCategories: ArrayList<Category>, path: String): String? {
        // search across categories first
        localCategories.forEach { cat ->
            if (cat.id == catId) {
                return path + cat.id + "/questionBanks/"
            }
        }
        // not found. Search one level deeper
        localCategories.forEach {
            if (it.subCategories != null) {
                return getCategoryPathById(catId, ArrayList(it.subCategories!!.values), path + it.id + "/subCategories/")
            }
        }
        return null
    }

    fun addQuestionBank(selectedPath: String, questionBank: StudyMaterial) {
        val catsDatabase = getCatsDatabase()
        val dept = catsDatabase.child("cats")
        dept.child(selectedPath).child(questionBank.id.toString()).setValue(questionBank)
    }

    fun getCourseById(courseId: Long?, callback: (course: Course?) -> Unit) {
        if (courseId != null) {
            getCats {
                callback(searchCourseById(courseId, it))
            }
        }
    }

    private fun searchCourseById(courseId: Long, localCategories: ArrayList<Category>): Course? {
        // search across categories first
        localCategories.forEach { cat ->
            if (cat.courses != null) {
                cat.courses!!.values.forEach { course ->
                    if(course != null) {
                        if(course.id == courseId) {
                            return course
                        }
                    }
                }
            }
        }
        // not found. Search one level deeper
        localCategories.forEach {
            if (it.subCategories != null) {
                val course = searchCourseById(courseId, ArrayList(it.subCategories!!.values))
                if (course != null) {
                    return course
                }
            }
        }
        return null
    }

    fun getCourseStudyMaterialById(courseId: Long, sectionId: Long, studyMaterialId: Long, callback: (course: StudyMaterial?) -> Unit) {
        getCourseById(courseId) {
            if(it != null && !it.sections.isEmpty()) {
                callback(it.sections.values.find { section -> section.id == sectionId }?.studyMaterials?.values?.find { studyMat -> studyMat.id == studyMaterialId })
            }
        }
    }

    fun addPurchaseLog(purLog: PurchaseLog) {
        val dbRef = getPurchaseLogDatabase()
        dbRef.child(Calendar.getInstance().time.time.toString()).setValue(purLog)
    }

    private fun getPurchaseLogDatabase(): DatabaseReference {
        return getDatabase().getReference("purchaseLog/$purchaseLogDbVersion")
    }

    fun updateCourseProperties(course: Course) {
        val catsDatabase = getCatsDatabase()
        val dept = catsDatabase.child("cats")
        dept.child("/1/courses/"+course.id).child("name").setValue(course.name)
        dept.child("/1/courses/"+course.id).child("desc").setValue(course.desc)
        dept.child("/1/courses/"+course.id).child("shortName").setValue(course.shortName)
        dept.child("/1/courses/"+course.id).child("purchaseInfo").setValue(course.purchaseInfo)
        dept.child("/1/courses/"+course.id).child("image").setValue(course.image)
        dept.child("/1/courses/"+course.id).child("order").setValue(course.order)
        dept.child("/1/courses/"+course.id).child("visible").setValue(course.isVisible)
    }

    fun updateCourseOrder(course: Course) {
        val catsDatabase = getCatsDatabase()
        val dept = catsDatabase.child("cats")
        dept.child("/1/courses/"+course.id).child("order").setValue(course.order)
    }

    fun deleteCourse(course: Category) {
        val catsDatabase = getCatsDatabase()
        val dept = catsDatabase.child("cats")
        dept.child("/1/courses/" + course.id).removeValue({ databaseError, _ ->
            val error = databaseError
        })
    }

    fun deleteCourseSection(courseId: Long, section: CourseSection) {
        val catsDatabase = getCatsDatabase()
        val dept = catsDatabase.child("cats")
        dept.child("/1/courses/" + courseId + "/sections/" + section.id).removeValue(DatabaseReference.CompletionListener { databaseError, databaseReference ->
            val error = databaseError
        })
    }

    fun updateCourseSectionProperties(courseId: Long?, section: CourseSection) {
        val catsDatabase = getCatsDatabase()
        val dept = catsDatabase.child("cats")
        dept.child("/1/courses/"+courseId + "/sections/" + section.id).child("name").setValue(section.name)
        dept.child("/1/courses/"+courseId + "/sections/" + section.id).child("desc").setValue(section.desc)
        dept.child("/1/courses/"+courseId + "/sections/" + section.id).child("purchaseInfo").setValue(section.purchaseInfo)
        dept.child("/1/courses/"+courseId + "/sections/" + section.id).child("image").setValue(section.image)
        dept.child("/1/courses/"+courseId + "/sections/" + section.id).child("order").setValue(section.order)
        dept.child("/1/courses/"+courseId + "/sections/" + section.id).child("visible").setValue(section.isVisible)
    }

    fun updateCourseSectionOrder(courseId: Long?, section: CourseSection) {
        val catsDatabase = getCatsDatabase()
        val dept = catsDatabase.child("cats")
        dept.child("/1/courses/"+courseId + "/sections/" + section.id).child("order").setValue(section.order)
    }

    fun updateQuizProperties(courseId: Long?, sectionId: Long, studyMaterial: StudyMaterial) {
        val catsDatabase = getCatsDatabase()
        val dept = catsDatabase.child("cats")
        dept.child("/1/courses/"+courseId + "/sections/" + sectionId + "/studyMaterials/" + studyMaterial.id).child("name").setValue(studyMaterial.name)
        dept.child("/1/courses/"+courseId + "/sections/" + sectionId + "/studyMaterials/" + studyMaterial.id).child("desc").setValue(studyMaterial.desc)
        dept.child("/1/courses/"+courseId + "/sections/" + sectionId + "/studyMaterials/" + studyMaterial.id).child("image").setValue(studyMaterial.image)
        dept.child("/1/courses/"+courseId + "/sections/" + sectionId + "/studyMaterials/" + studyMaterial.id).child("purchaseInfo").setValue(studyMaterial.purchaseInfo)
        dept.child("/1/courses/"+courseId + "/sections/" + sectionId + "/studyMaterials/" + studyMaterial.id).child("order").setValue(studyMaterial.order)
        dept.child("/1/courses/"+courseId + "/sections/" + sectionId + "/studyMaterials/" + studyMaterial.id).child("visible").setValue(studyMaterial.isVisible)
        dept.child("/1/courses/"+courseId + "/sections/" + sectionId + "/studyMaterials/" + studyMaterial.id).child("randomizeQuestions").setValue(studyMaterial.randomizeQuestions)
        dept.child("/1/courses/"+courseId + "/sections/" + sectionId + "/studyMaterials/" + studyMaterial.id).child("noQuestionsInQuiz").setValue(studyMaterial.noQuestionsInQuiz)
    }

    fun updateQuizOrder(courseId: Long?, sectionId: Long, studyMaterial: StudyMaterial) {
        val catsDatabase = getCatsDatabase()
        val dept = catsDatabase.child("cats")
        dept.child("/1/courses/"+courseId + "/sections/" + sectionId + "/studyMaterials/" + studyMaterial.id).child("order").setValue(studyMaterial.order)
    }

    fun updateQuizQuestions(courseId: Long, sectionId: Long, studyMaterial: StudyMaterial) {
        val catsDatabase = getCatsDatabase()
        val dept = catsDatabase.child("cats")
        dept.child("/1/courses/"+courseId + "/sections/" + sectionId + "/studyMaterials/" + studyMaterial.id).child("questions").setValue(studyMaterial.questions)
    }

    fun updateVideoProperties(courseId: Long, sectionId: Long, studyMaterial: StudyMaterial) {
        val catsDatabase = getCatsDatabase()
        val dept = catsDatabase.child("cats")
        dept.child("/1/courses/"+courseId + "/sections/" + sectionId + "/studyMaterials/" + studyMaterial.id).child("videos").setValue(studyMaterial.videos)
    }

    fun addCourseSection(courseId: Long?, section: CourseSection) {
        val catsDatabase = getCatsDatabase()
        val dept = catsDatabase.child("cats")
        dept.child("/1/courses/"+courseId + "/sections/" + section.id).setValue(section)
    }

    fun addCourse(course: Course) {
        val catsDatabase = getCatsDatabase()
        val dept = catsDatabase.child("cats")
        dept.child("/1/courses/" + course.id).setValue(course)
    }

    fun addStudyMaterial(courseId: Long?, sectionId: Long, studyMaterial: StudyMaterial) {
        val catsDatabase = getCatsDatabase()
        val dept = catsDatabase.child("cats")
        dept.child("/1/courses/"+courseId + "/sections/" + sectionId + "/studyMaterials/" + studyMaterial.id).setValue(studyMaterial)
    }

    fun deleteStudyMaterial(courseId: Long?, sectionId: Long, studyMaterial: StudyMaterial) {
        val catsDatabase = getCatsDatabase()
        val dept = catsDatabase.child("cats")
        dept.child("/1/courses/"+courseId + "/sections/" + sectionId + "/studyMaterials/" + studyMaterial.id).removeValue()
    }

    fun getUsers(callback: (cats: ArrayList<User>) -> Unit) {
        val database = getUsersDatabase()
        val dept = database.child("users")
        dept.keepSynced(true)

        dept.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
//                    Log.d("Xz_", "getCats listener:"+System.nanoTime())
                val genericTypeIndicator = object : GenericTypeIndicator<HashMap<String, User>>() {}
                val users = snapshot.getValue(genericTypeIndicator)
                if (users != null) {
                    var users = ArrayList(users.values)
                    users = users.filter { it != null } as ArrayList<User>
                    callback(users)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("The read failed: " + databaseError.code)
            }
        })
    }

    fun deleteUser(user: User) {
        val database = getUsersDatabase()
        database.child("users").child(user.getId()).removeValue().addOnFailureListener {
            val error = it
        }
    }

    fun updateUser(user: User) {
        val database = getUsersDatabase()
        database.child("users").child(user.getId() + "/userType").setValue(user.userType)
        database.child("users").child(user.getId() + "/name").setValue(user.name)
    }

    fun getSectionPathById(sectionId: Long, callback: (path: String?) -> Unit) {
        getCats {
            var path = "/"
            callback(searchSectionPathById(sectionId, it, path))
        }
    }

    private fun searchSectionPathById(sectionId: Long, localCategories: ArrayList<Category>, path: String): String? {
        // search across categories first
        localCategories.forEach { cat ->
            if (cat.courses != null) {
                cat.courses!!.values.forEach { course ->
                    course?.sections.values.forEach {
                        if(it != null && it.id == sectionId) {
                            return path + cat.id + "/courses/" + course.id + "/sections/" + it.id
                        }
                    }
                }
            }
        }
        // not found. Search one level deeper
        localCategories.forEach {
            if (it.subCategories != null) {
                val retVal = searchSectionPathById(sectionId, ArrayList(it.subCategories!!.values), path + it.id + "/subCategories/")
                if(retVal != null) {
                    return retVal
                }
            }
        }
        return null
    }

    fun updateCourseImages(course: Course) {
        val catsDatabase = getCatsDatabase()
        val dept = catsDatabase.child("cats")
        dept.child("/1/courses/"+course.id).child("descImages").setValue(course.descImages)
    }
}