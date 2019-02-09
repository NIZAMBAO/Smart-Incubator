package com.v1rex.smartincubator.Activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton

import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.v1rex.smartincubator.Model.Mentor
import com.v1rex.smartincubator.Model.Startup
import com.v1rex.smartincubator.R
import com.v1rex.smartincubator.ViewHolder.MentorViewHolder
import com.v1rex.smartincubator.ViewHolder.StartupViewHolder
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {

    private var mReferenceStartups: DatabaseReference? = null
    private var StartupfirebaseRecyclerAdapter: FirebaseRecyclerAdapter<Startup, StartupViewHolder>? = null
    private var Startupoptions: FirebaseRecyclerOptions<Startup>? = null

    private var mReferenceMentors: DatabaseReference? = null
    private var MentorfirebaseRecyclerAdapter: FirebaseRecyclerAdapter<Mentor, MentorViewHolder>? = null
    private var Mentoroptions: FirebaseRecyclerOptions<Mentor>? = null

    private lateinit var startupLinearLayoutManager: LinearLayoutManager
    private lateinit var mentorLinearLayoutManager: LinearLayoutManager
    private lateinit var startupAdapter: StartupAdapter
    private lateinit var mentorAdapter: MentorAdapter

    private var searchText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        var number : Int = 0
        linearlayout_startup_search.visibility = View.GONE
        linearlayout_mentor_search.visibility = View.GONE

        // setting the return button manually
        search_back_button.setOnClickListener {
            startActivity(Intent(this@SearchActivity, BottonNavigationActivity::class.java))
            finish()
        }

        startupLinearLayoutManager = LinearLayoutManager(this)
        mentorLinearLayoutManager = LinearLayoutManager(this)
        search_startups_recyclerview.layoutManager = startupLinearLayoutManager
        search_mentors_recyclerview.layoutManager = mentorLinearLayoutManager


        var startupList : ArrayList<Startup> = ArrayList<Startup>()
        mReferenceStartups = FirebaseDatabase.getInstance().reference.child("Data").child("startups")

        val valueEventListenerStartup = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    var data : Startup? = postSnapshot.getValue<Startup>(Startup::class.java)
                    startupList.add(data!!)
                }

                startupAdapter = StartupAdapter(startupList, baseContext)
                search_startups_recyclerview.adapter = startupAdapter



            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }

        mReferenceStartups!!.addListenerForSingleValueEvent(valueEventListenerStartup)



        var mentorList : ArrayList<Mentor> = ArrayList<Mentor>()
        mReferenceMentors = FirebaseDatabase.getInstance().reference.child("Data").child("mentors")

        val valueEventListenerMentor = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    var data : Mentor? = postSnapshot.getValue<Mentor>(Mentor::class.java)
                    mentorList.add(data!!)
                }

                //startupAdapter = StartupAdapter(startupList, baseContext)
                mentorAdapter = MentorAdapter(mentorList, baseContext)
                search_mentors_recyclerview.adapter = mentorAdapter



            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }

        mReferenceMentors!!.addListenerForSingleValueEvent(valueEventListenerMentor)


        if (startup_radio_button_search.isChecked == true) {
            linearlayout_startup_search.visibility = View.VISIBLE
            number = 0
        } else{
            linearlayout_mentor_search.visibility = View.VISIBLE
            number = 1
        }

        startup_radio_button_search.setOnClickListener {
            linearlayout_startup_search.visibility = View.VISIBLE
            linearlayout_mentor_search.visibility = View.GONE
            number = 0
        }

        mentor_radio_button_search.setOnClickListener {
            linearlayout_startup_search.visibility = View.GONE
            linearlayout_mentor_search.visibility = View.VISIBLE
            number = 1
        }



        /**
         * search between startups or mentors
         * @param search
         */
        fun search(search: String?) {
            var startupListFinal : ArrayList<Startup> = ArrayList<Startup>()
            var mentorListFinal : ArrayList<Mentor> = ArrayList<Mentor>()
            if(number == 0){
                for(startupListSearch in startupList)  {
                    if ((startupListSearch.mNeed.contains(search.toString()) || startupListSearch.mDomain.contains(search.toString())  || startupListSearch.mStartupName.contains(search.toString())) || (startupListSearch.mNeed.equals(search.toString(), ignoreCase = true) || startupListSearch.mDomain.equals(search.toString(), ignoreCase = true)  || startupListSearch.mStartupName.equals(search.toString(), ignoreCase = true)) ){
                        startupListFinal.add(startupListSearch)

                    }
                }
                startupAdapter = StartupAdapter(startupListFinal, baseContext)
                search_startups_recyclerview.adapter = startupAdapter

            } else if(number == 1){
                for(mentorListSearch in mentorList){
                    if ((mentorListSearch.mSpeciality.contains(search.toString())  || mentorListSearch.mCity.contains(search.toString())  || mentorListSearch.mFirstName.contains(search.toString())   || mentorListSearch.mLastName.contains(search.toString())  ) || (mentorListSearch.mSpeciality.equals(search.toString(), ignoreCase = true)  || mentorListSearch.mCity.equals(search.toString(), ignoreCase = true)  || mentorListSearch.mFirstName.equals(search.toString(), ignoreCase = true)   || mentorListSearch.mLastName.equals(search.toString() , ignoreCase = true)  )){
                        mentorListFinal.add(mentorListSearch)
                    }
                }
                mentorAdapter = MentorAdapter(mentorListFinal, baseContext)
                search_mentors_recyclerview.adapter = mentorAdapter

            }


        }





        search_view_activity.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchText = search_view_activity.query.toString()
                search(searchText)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return true
            }
        })



    }





    class StartupAdapter(private val startups : ArrayList<Startup>, private var context : Context) : RecyclerView.Adapter<StartupViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StartupViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_startups_layout, parent, false)

            return StartupViewHolder(view)
        }

        override fun getItemCount(): Int = startups.size

        override fun onBindViewHolder(holder: StartupViewHolder, position: Int) {
            holder.setmNeedTextView("Need :" + " " + startups.get(position).mNeed)
            holder.setmNameTextView(startups.get(position).mStartupName)
            holder.setmDomainTextView("Domain :" + " " + startups.get(position).mDomain)

            holder.itemView.setOnClickListener {
                val intent = Intent(context, StartupProfileActivity::class.java)
                intent.putExtra("UserId Startup", startups.get(position).mUserId)
                context.startActivity(intent)
            }
        }

    }

    class MentorAdapter(private val mentors : ArrayList<Mentor>, private var context : Context) : RecyclerView.Adapter<MentorViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentorViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_mentors_layout, parent, false)

            return MentorViewHolder(view)
        }

        override fun getItemCount(): Int = mentors.size

        override fun onBindViewHolder(holder: MentorViewHolder, position: Int) {
            holder.setmNameTextView(mentors.get(position).mLastName + " " + mentors.get(position).mFirstName)
            holder.setmCityTextView(mentors.get(position).mCity)
            holder.setmSpecialityTextView(mentors.get(position).mSpeciality)


            holder.itemView.setOnClickListener {
                val intent = Intent(context, MentorProfileActivity::class.java)
                intent.putExtra("Mentor userId", mentors.get(position).mUserId)
                context.startActivity(intent)
            }
        }

    }
}
