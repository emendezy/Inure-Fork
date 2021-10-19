package app.simple.inure.popups.viewers

import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.adapters.popups.AdapterGraphicsFilter
import app.simple.inure.decorations.popup.BasePopupWindow
import app.simple.inure.decorations.popup.PopupLinearLayout
import app.simple.inure.decorations.views.CustomVerticalRecyclerView

class PopupGraphicsFilter(anchor: View) : BasePopupWindow() {

    private var recyclerView: CustomVerticalRecyclerView

    init {
        val contentView = LayoutInflater.from(anchor.context).inflate(R.layout.popup_graphics_filter, PopupLinearLayout(anchor.context))

        recyclerView = contentView.findViewById(R.id.graphics_filter_recycler_view)
        recyclerView.adapter = AdapterGraphicsFilter()

        init(contentView, anchor)
    }
}