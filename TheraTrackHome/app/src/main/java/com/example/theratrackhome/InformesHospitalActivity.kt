package com.example.theratrackhome

import android.os.Bundle

class InformesHospitalActivity : BaseHospitalActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informes_hospital)
        configurarNavegacion(R.id.nav_informes)
    }
}
