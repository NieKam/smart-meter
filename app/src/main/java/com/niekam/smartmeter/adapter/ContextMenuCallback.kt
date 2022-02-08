package com.niekam.smartmeter.adapter


interface ContextMenuCallback<T> {
    fun onEdit(obj: T)

    fun onDelete(obj: T)
}