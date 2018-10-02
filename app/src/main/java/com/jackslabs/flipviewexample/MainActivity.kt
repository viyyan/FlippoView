package com.jackslabs.flipviewexample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.alfianyulianto.flippoview.FlippoView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn_front = front_card.findViewById<Button>(R.id.front_btn)
        val btn_back = back_card.findViewById<Button>(R.id.back_btn)

        btn_front.setOnClickListener{
            Log.d("btn_click", "btn_front")
            flippo_view.startFlip()
        }

        btn_back.setOnClickListener {
            Log.d("btn_click", "btn_back")
            flippo_view.startFlip()
        }

        flippo_view.setOnFlipAnimationListener(object : FlippoView.OnFlipAnimationListener {
            override fun onFlipDone(flippoView: FlippoView, sideState: FlippoView.SideState) {
                Log.d("anim_listener", sideState.name.toString())
            }
        })
    }
}
