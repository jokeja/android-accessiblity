package com.example.myaccessiblityserv

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

class AccesNodeUtil {
    companion object {
        fun logAllNodes(
            parentNode: AccessibilityNodeInfo,
            space: String,
            canPerform: ((AccessibilityNodeInfo) -> Boolean)?
        ) {
            Log.e(space, parentNode.toString())
            for (index in 0..parentNode.childCount - 1) {
                var child = parentNode.getChild(index)
                if (child != null) {
                    logAllNodes(child, space + space, canPerform)
                }
            }
        }

        fun findNodeByText(
            parentNode: AccessibilityNodeInfo,
            space: Int,
            text: String?,
            showLog: Boolean = false
        ): AccessibilityNodeInfo? {
            var result: AccessibilityNodeInfo? = null
            if (showLog) {
                Log.e("" + space + "、parentNode==", parentNode.toString())
            }
            for (index in 0..parentNode.childCount - 1) {
                var child = parentNode.getChild(index)
                if (child != null) {
                    if (showLog) {
                        Log.e("" + space + "、child==", child.toString())
                    }
                    if (text != null && child.text != null && child.text == (text!!)) {
                        if (showLog) {
                            Log.e("" + space + "、parentNode==", parentNode.toString())
                        }
                        result = child
                    } else if (child.childCount > 0) {
                        result = findNodeByText(child, space + 1, text, showLog)
                    }
                }
                if (result != null) {
                    return result
                }
            }
            return result
        }

        fun findAllNodesByText(
            parentNode: AccessibilityNodeInfo,
            space: Int,
            text: String?,
            showLog: Boolean = false
        ): ArrayList<AccessibilityNodeInfo> {
            var result: ArrayList<AccessibilityNodeInfo> = ArrayList()
            if (showLog) {
                Log.e("" + space + "、parentNode==", parentNode.toString())
            }
            for (index in 0..parentNode.childCount - 1) {
                var child = parentNode.getChild(index)
                if (child != null) {
                    if (showLog) {
                        Log.e("" + space + "、child==", child.toString())
                    }
                    if (text != null && child.text != null && child.text == (text!!)) {
                        result.add(child)
                    } else if (child.childCount > 0) {
                        result.addAll(findAllNodesByText(child, space + 1, text, showLog))
                    }
                }
            }
            return result
        }


        fun findNodeById(
            parentNode: AccessibilityNodeInfo,
            space: Int,
            viewId: String?,
            showLog: Boolean = false
        ): AccessibilityNodeInfo? {
            var result: AccessibilityNodeInfo? = null
            for (index in 0..parentNode.childCount - 1) {
                var child = parentNode.getChild(index)
                if (child != null) {
                    if (viewId != null && child.viewIdResourceName != null && child.viewIdResourceName == (
                                viewId!!
                                )
                    ) {
                        result = child
                    } else if (child.childCount > 0) {
                        result = findNodeById(child, space + 1, viewId, showLog)
                    }
                    if (showLog) {
                        Log.e(
                            "parentNode==" + parentNode.viewIdResourceName + "" + "、child==",
                            child.toString()
                        )
                    }
                }
                if (result != null) {
                    return result
                }
            }
            return result
        }

        fun tapNode(service: AccessibilityService, parentNode: AccessibilityNodeInfo) {

            var path = Path()
            val rect = Rect()
            parentNode.getBoundsInScreen(rect)
            path.moveTo(
                Math.abs(rect!!.centerX().toFloat()),
                rect!!.centerY().toFloat()
            );//设置Path的起点
            var builder = GestureDescription.Builder()
            var stroke = GestureDescription.StrokeDescription(path, 0, 300, false)
            var callback = object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription) {
                    Log.e("------step--------", gestureDescription.toString())
                }

                override fun onCancelled(gestureDescription: GestureDescription) {
                    Log.e("------step--------", gestureDescription.toString())
                }
            }
            var dispatchGestureresult = service.dispatchGesture(
                builder.addStroke(stroke).build(),
                callback,
                null
            )
        }
    }
}