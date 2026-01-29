package com.example.foodapp.pages.shipper.notifications

import com.example.foodapp.data.model.owner.notification.Notification as OwnerNotification
import com.example.foodapp.data.model.owner.notification.NotificationType as OwnerNotificationType

// Reuse shared Notification models from owner API for shipper notifications.
typealias Notification = OwnerNotification
typealias NotificationType = OwnerNotificationType
