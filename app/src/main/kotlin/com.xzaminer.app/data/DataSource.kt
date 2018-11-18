package com.xzaminer.app.data

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.xzaminer.app.category.Category
import com.xzaminer.app.extensions.config
import com.xzaminer.app.quiz.QuestionBank
import java.util.*

private var db: FirebaseDatabase? = null
private var catsDbVersion = 1
private var userDbVersion = 1
private var storage: FirebaseStorage? = null

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
        return getDatabase().getReference("users/v$userDbVersion")
    }

    fun getStorage(): FirebaseStorage {
        if (storage == null) {
            storage = FirebaseStorage.getInstance()
        }

        return storage as FirebaseStorage
    }

    fun getCatsDatabase(): DatabaseReference {
        return getDatabase().getReference("cats/v$catsDbVersion")
    }

    fun getCategories(catId: Long?, callback: (cats: ArrayList<Category>, name: String) -> Unit) {
        if (catId != null) {
            getCategoryById(catId) {
                val cats: ArrayList<Category> = arrayListOf()
                if (it?.subCategories != null) {
                    cats.addAll(ArrayList(it.subCategories!!.values))
                }

                if (it?.questionBanks != null) {
                    val audToCat: ArrayList<Category> = ArrayList()
                    if (it.questionBanks != null) {
                        it.questionBanks!!.values.forEach {
                            if (it != null) {
                                audToCat.add(Category(it.id, it.name, it.description, it.imageIcon, null, null, true))
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
        if (cats != null) {
            callback(cats!!)
        } else {
            val database = getCatsDatabase()
            val dept = database.child("cats")
            dept.keepSynced(true)

            dept.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val genericTypeIndicator = object : GenericTypeIndicator<ArrayList<Category>>() {
                    }
                    var categories = snapshot.getValue(genericTypeIndicator)
                    if (categories != null) {
                        categories = categories.filter { it != null } as ArrayList<Category>
                        cats = categories
                        callback(categories)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("The read failed: " + (databaseError.code ?: ""))
                }
            })

            dept.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val genericTypeIndicator = object : GenericTypeIndicator<ArrayList<Category>>() {
                    }
                    var categories = snapshot.getValue(genericTypeIndicator)
                    if (categories != null) {
                        categories = categories!!.filter { it != null } as ArrayList<Category>
                        cats = categories
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("The read failed: " + (databaseError?.code ?: ""))
                }
            })
        }
    }

    private fun getCategoryById(catId: Long, callback: (cat: Category?) -> Unit) {
        getCats { it ->
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

    fun getQuestionBank(qbId: Long?, callback: (questionBank: QuestionBank?) -> Unit) {
        if (qbId != null) {
            getCats { it ->
                callback(searchQuestionBankById(qbId, it))
            }
        }
    }

    private fun searchQuestionBankById(questionBankId: Long, localCategories: ArrayList<Category>): QuestionBank? {
        // search across categories first
        localCategories.forEach { cat ->
            if (cat.questionBanks != null) {
                cat.questionBanks!!.values.forEach { questionBank ->
                    if(questionBank != null) {
                        if(questionBank.id == questionBankId) {
                            return questionBank
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

    fun getQuestionBankFromUser(userId: String, quizId: Long?, callback: (quiz: QuestionBank?) -> Unit) {
        getUser(userId) {
            val quiz = it?.quizzes?.find { it.id == quizId }
            callback(quiz)
        }
    }
}