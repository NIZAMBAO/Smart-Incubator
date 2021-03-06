package com.v1rex.smartincubator.Fragments

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.v1rex.smartincubator.Activities.MentorProfileActivity
import com.v1rex.smartincubator.Activities.StartupProfileActivity
import com.v1rex.smartincubator.Model.Meeting
import com.v1rex.smartincubator.Model.User
import com.v1rex.smartincubator.R
import com.v1rex.smartincubator.ViewHolder.MeetingsViewHolder
import java.text.SimpleDateFormat
import java.util.*


class MeetingsFragment : Fragment() {

    private var mList: RecyclerView? = null
    private var mLoaderMessage: LinearLayout? = null
    private var mReference: DatabaseReference? = null
    private var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<Meeting, MeetingsViewHolder>? = null
    private var options: FirebaseRecyclerOptions<Meeting>? = null
    private var mAuth: FirebaseAuth? = null

    private val databaseMeetings = FirebaseDatabase.getInstance()
    private var ref = databaseMeetings.getReference("Data")
    private var user: User? = null

    private var mEmptyMeetingsText: TextView? = null

    private var mLinearLayoutReceived: LinearLayout? = null
    private var mExitReceived : ImageButton? = null
    private var mUpdateReceived : ImageButton? = null
    private var mReceivedUserName: TextView? = null
    private var mReceivedEmail: TextView? = null
    private var mReceivedUserType: TextView? = null
    private var mSeeProfileReceived: TextView? = null
    private var mAcceptButton: RadioButton? = null
    private var mRefuseButton: RadioButton? = null


    private var mLinearLayoutSented: LinearLayout? = null
    private var mExitSented : ImageButton? = null
    private var mUpdateSented : ImageButton? = null
    private var mInputMeetingSendtedTextLayout : TextInputLayout? = null
    private var mMeetingPlaceEditText : EditText? = null
    private var mSetDateSentedBtn : Button? = null
    private var mSetTimeSentedBtn : Button? = null
    private var mLinearLayoutDateSented: LinearLayout? = null
    private var mFinishedDateSented : ImageButton? = null
    private var mLinearLayoutTimeSented: LinearLayout? = null
    private var mFinishedTimeSented : ImageButton? = null
    private var mDateSented : DatePicker? = null
    private var mTimeSented : TimePicker? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val userId: String

        val view  : View? = inflater.inflate(R.layout.fragment_meetings, container, false)


        // setting the layout that will be showed if the user click on a meeting that he sented
        //it will make him able to change the informations about the meeting
        mLinearLayoutSented = view!!.findViewById<View>(R.id.meeting_linearlayout_sented) as LinearLayout

        mExitSented = view!!.findViewById<View>(R.id.exit_mettings_sented) as ImageButton
        mUpdateSented = view!!.findViewById<View>(R.id.send_meetings_sented) as ImageButton

        mInputMeetingSendtedTextLayout = view!!.findViewById<TextInputLayout>(R.id.input_layout_place_meeting_sented) as TextInputLayout

        mMeetingPlaceEditText = view!!.findViewById<EditText>(R.id.meeting_place_edit_text_sented) as EditText

        mSetDateSentedBtn = view!!.findViewById<Button>(R.id.set_date_btn_sented) as Button
        mSetTimeSentedBtn = view!!.findViewById<Button>(R.id.set_time_btn_sented) as Button


        mLinearLayoutDateSented = view!!.findViewById<View>(R.id.date_picker_layout_sented) as LinearLayout
        mFinishedDateSented = view!!.findViewById<View>(R.id.finished_date_sented) as ImageButton

        mLinearLayoutTimeSented = view!!.findViewById<View>(R.id.time_picker_layout_sented) as LinearLayout
        mFinishedTimeSented = view!!.findViewById<View>(R.id.finished_time_sented) as ImageButton

        mDateSented = view!!.findViewById<DatePicker>(R.id.date_picker_sented) as DatePicker
        mTimeSented = view!!.findViewById<TimePicker>(R.id.time_picker_sented) as TimePicker




        // setting the layout that will be showed if the user click on a meeting that he received
        //it will make him able to see the profile of the sender and accept or refuse the meeting
        mLinearLayoutReceived = view!!.findViewById<View>(R.id.linearlayout_received) as LinearLayout

        mExitReceived = view!!.findViewById<View>(R.id.exit_received) as ImageButton
        mUpdateReceived = view!!.findViewById<View>(R.id.update_received) as ImageButton

        mReceivedUserName = view!!.findViewById<View>(R.id.received_user_name) as TextView
        mReceivedEmail = view!!.findViewById<View>(R.id.received_user_email) as TextView
        mReceivedUserType = view!!.findViewById<View>(R.id.received_user_type) as TextView
        mSeeProfileReceived = view!!.findViewById<View>(R.id.see_profile_received) as TextView

        mAcceptButton = view!!.findViewById<View>(R.id.accept_received_button) as RadioButton
        mRefuseButton = view!!.findViewById<View>(R.id.refuse_received_button) as RadioButton


        //this layout is showed if the user have no meetings
        mEmptyMeetingsText = view!!.findViewById<View>(R.id.empty_meetings_message) as TextView
        mEmptyMeetingsText!!.visibility = View.GONE


        // getting Auth firebase instance
        mAuth = FirebaseAuth.getInstance()


        //setting where to find meetings informations
        mReference = FirebaseDatabase.getInstance().reference.child("Data").child("Meetings").child(mAuth!!.uid!!)
        mReference!!.keepSynced(true)

        // showed a waiting loader
        mLoaderMessage = view!!.findViewById<View>(R.id.message_load_progress) as LinearLayout
        mLoaderMessage!!.visibility = View.VISIBLE

        //showing the meetings in a recyclerview
        mList = view!!.findViewById<View>(R.id.meetings_recyclerview) as RecyclerView
        mList!!.setHasFixedSize(true)
        mList!!.layoutManager = LinearLayoutManager(this.activity)


        options = FirebaseRecyclerOptions.Builder<Meeting>().setQuery(mReference!!, Meeting::class.java!!).build()

        // setting the firebaseRecyclerAdapter for the showing meetings
        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Meeting, MeetingsViewHolder>(options!!) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeetingsViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_meetings, parent, false)
                return MeetingsViewHolder(view)
            }

            override fun onDataChanged() {
                super.onDataChanged()
                if(itemCount == 0){
                    mLoaderMessage!!.visibility = View.GONE
                    mEmptyMeetingsText!!.visibility = View.VISIBLE

                }
            }


            override fun onBindViewHolder(holder: MeetingsViewHolder, position: Int, model: Meeting) {
                    mLoaderMessage!!.visibility = View.GONE
                    mEmptyMeetingsText!!.visibility = View.GONE
                    holder.setmPlaceTextView(model.mPlace)
                    holder.setmTypeEditText(model.mType)
                    holder.setmDateEditText(model.mDate)
                    holder.setmStatusEditText(model.accepte)
                    val userId = model.mUserIdSent

                    // open meetings informations
                    holder.itemView.setOnClickListener {
                        // if the user received the meeting then show this
                        if (model.mType == "You received") {
                            // Loading user informations who sent the meetings
                            val valueEventListenerMeeting = object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    // Store user informations on a object
                                    user = dataSnapshot.child(userId!!).getValue<User>(User::class.java)


                                    if (model.accepte == "accepted") {
                                        mAcceptButton!!.isChecked = true
                                    } else if (model.accepte == "refused") {
                                        mRefuseButton!!.isChecked = true
                                    }
                                    mLinearLayoutReceived!!.visibility = View.VISIBLE
                                    mReceivedUserName!!.setText("UserName: " + user!!.mFirstName + " " + user!!.mLastName)
                                    mReceivedEmail!!.setText( "User Email: " + user!!.mEmail)
                                    mReceivedUserType!!.setText("User Type: " + user!!.mAccountType)

                                    mSeeProfileReceived!!.setOnClickListener {
                                        if (user!!.mAccountType == "Startup") {
                                            // Open the mentors profile who sent the meetings
                                            val intent = Intent(activity, StartupProfileActivity::class.java)
                                            intent.putExtra("UserId Startup", user!!.mUserId)
                                            startActivity(intent)
                                        } else if (user!!.mAccountType == "Mentor") {
                                            // Open the mentors profile who sent the meetings
                                            val intent = Intent(activity, MentorProfileActivity::class.java)
                                            intent.putExtra("Mentor userId", user!!.mUserId)
                                            startActivity(intent)
                                        }
                                    }

                                    mExitReceived!!.setOnClickListener { mLinearLayoutReceived!!.visibility = View.INVISIBLE }

                                    // Update data if the user clicked on the green check button
                                    mUpdateReceived!!.setOnClickListener {
                                        //if accepted
                                        if (mAcceptButton!!.isChecked == true) {
                                            ref = databaseMeetings.getReference("Data")
                                            // Store meeting informations in object Meeting
                                            val meeting = Meeting(model.mUserIdSent.toString(), model.mUserIdReceived.toString(), model.mPlace.toString(), model.mDate.toString(), "accepted", model.mType.toString())

                                            //Settings where to update meetings informations in the part of the part who sent
                                            val userRef2 = ref.child("Meetings").child(meeting.mUserIdSent).child(meeting.mUserIdReceived)
                                            // Update meeting
                                            meeting.mType = "You sented"
                                            userRef2.setValue(meeting)

                                            //Settings where to update meetings informations in the part of the part who sent
                                            val userRef = ref.child("Meetings").child(meeting.mUserIdReceived).child(meeting.mUserIdSent)
                                            // Update meeting
                                            meeting.mType = "You received"
                                            userRef.setValue(meeting)
                                            mLinearLayoutReceived!!.visibility = View.GONE
                                        } else if (mRefuseButton!!.isChecked == true) {
                                            // Store meeting informations in object Meeting
                                            val meeting = Meeting(model.mUserIdSent.toString(), model.mUserIdReceived.toString(), model.mPlace.toString(), model.mDate.toString(), "refused", model.mType.toString())
                                            //Settings where to update meetings informations in the part of the part who sent
                                            val userRef2 = ref.child("Meetings").child(meeting.mUserIdSent).child(meeting.mUserIdReceived)
                                            // Update meeting
                                            userRef2.setValue(meeting)

                                            //Settings where to update meetings informations in the part of the part who sent
                                            val userRef = ref.child("Meetings").child(meeting.mUserIdReceived).child(meeting.mUserIdSent)
                                            // Update meeting
                                            userRef.setValue(meeting)
                                            mLinearLayoutReceived!!.visibility = View.GONE

                                        } else {
                                            // if the user who received the meetings didn't update anything so don't send any data to the server
                                            mLinearLayoutReceived!!.visibility = View.GONE
                                        }
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {

                                }
                            }

                            ref = databaseMeetings.getReference("Data").child("users")
                            ref.addValueEventListener(valueEventListenerMeeting)


                        } else if (model.mType == "You sented") {


                            mMeetingPlaceEditText!!.setText(model.mPlace)
                            var dateTime = model.mDate
                            var cal = Calendar.getInstance()
                            cal.time = SimpleDateFormat("yyyy.MM.dd 'at' hh:mm").parse(dateTime)
                            var date = SimpleDateFormat("yyyy.MM.dd 'at' hh:mm").parse(dateTime)
                            mDateSented!!.updateDate(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                            mTimeSented!!.currentHour = date.hours
                            mTimeSented!!.currentMinute = date.minutes





                            mLinearLayoutSented!!.visibility = View.VISIBLE

                            mExitSented!!.setOnClickListener {
                                mLinearLayoutSented!!.visibility = View.GONE
                            }



                            mSetDateSentedBtn!!.setOnClickListener {
                                mLinearLayoutDateSented!!.visibility = View.VISIBLE
                            }

                            mSetTimeSentedBtn!!.setOnClickListener {
                                mLinearLayoutTimeSented!!.visibility = View.VISIBLE
                            }

                            mFinishedDateSented!!.setOnClickListener {
                                mLinearLayoutDateSented!!.visibility = View.GONE
                            }

                            mFinishedTimeSented!!.setOnClickListener {
                                mLinearLayoutTimeSented!!.visibility = View.GONE
                            }

                            mUpdateSented!!.setOnClickListener {

                                var place = mMeetingPlaceEditText!!.text.toString()

                                if (TextUtils.isEmpty(place)) {
                                    mInputMeetingSendtedTextLayout!!.error = getString(R.string.field_requierd)
                                }else{
                                    val day = mDateSented!!.dayOfMonth
                                    val month = mDateSented!!.month
                                    val year = mDateSented!!.year
                                    val hour = mTimeSented!!.currentHour
                                    val minute = mTimeSented!!.currentMinute

                                    val calendar = Calendar.getInstance()
                                    calendar.set(year, month, day, hour, minute)
                                    val date = SimpleDateFormat("yyyy.MM.dd 'at' hh:mm")
                                    val dateAndTime = date.format(calendar.time)

                                    // creating a meeting object for the user who will receive the meeting
                                    val meeting = Meeting(mAuth!!.uid.toString(), model.mUserIdReceived, place, dateAndTime, model.accepte, model.mType)
                                    var referrence = databaseMeetings.getReference("Data")
                                    val usersRef = referrence.child("Meetings")
                                    val userRef = usersRef.child(meeting.mUserIdSent).child(meeting.mUserIdReceived)
                                    userRef.setValue(meeting)

                                    mLinearLayoutSented!!.visibility = View.GONE

                                    meeting.mType = "You received"
                                    var userRef1 = referrence.child("Meetings")
                                    userRef1 = userRef1.child(meeting.mUserIdReceived).child(meeting.mUserIdSent)
                                    userRef1.setValue(meeting)
                                }
                            }
                        }
                    }

            }


        }

        mList!!.adapter = firebaseRecyclerAdapter

        return view
    }

    // Listening for changes in the Realtime Database
    override fun onStart() {
        super.onStart()
        firebaseRecyclerAdapter!!.startListening()
    }

    // Stop listening for changes in the Realtime Database
    override fun onStop() {
        super.onStop()
        firebaseRecyclerAdapter!!.stopListening()
    }
}
