package com.xzaminer.app.course

import ss.com.bannerslider.adapters.SliderAdapter
import ss.com.bannerslider.viewholder.ImageSlideViewHolder


class CourseDescriptionImageAdapter(var images: ArrayList<String>) : SliderAdapter() {

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindImageSlide(position: Int, viewHolder: ImageSlideViewHolder) {
        when (position) {
            position -> viewHolder.bindImageSlide(images[position])
        }
    }
}