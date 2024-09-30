package com.pro.shopfee.event

import com.pro.shopfee.model.Address

class AddressSelectedEvent(var address: Address, var lat: Double, var lng: Double)