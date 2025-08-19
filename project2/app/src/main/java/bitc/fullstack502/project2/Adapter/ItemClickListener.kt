package bitc.fullstack502.project2.Adapter

import bitc.fullstack502.project2.FoodItem

// Adapter에서 아이템 클릭 이벤트를 처리하기 위한 인터페이스
interface ItemClickListener {
    fun onItemClick(item: FoodItem)
}