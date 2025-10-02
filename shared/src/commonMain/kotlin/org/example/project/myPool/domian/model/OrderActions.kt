package org.example.project.myPool.domian.model

sealed class OrderActions {
    data object Confirm : OrderActions()
    data object PickUp : OrderActions()
    data object Start : OrderActions()
    data object Deliver : OrderActions()
    data object Fail : OrderActions()
}