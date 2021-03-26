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
            canPerform: ((AccessibilityNodeInfo) -> Boolean)?,
            tag: String = ""
        ) {
            for (index in 0..parentNode.childCount - 1) {
                var child = parentNode.getChild(index)
                if (child != null) {
                    val ntag = Integer.toHexString(parentNode.hashCode()) + tag
                    var outrect = Rect()
                    child.getBoundsInScreen(outrect)
                    Log.e(
                        ntag,
                        "hashCode:${Integer.toHexString(child.hashCode())},text:${child.text},viewIdRes:${child.viewIdResourceName},BoundsInScreen${outrect},packageName:${child.packageName},className:${child.className}"
                    )
                    logAllNodes(child, canPerform, "," + ntag)
                }
            }
        }

        fun findNodeByText(
            parentNode: AccessibilityNodeInfo,
            space: Int,
            text: String?,
            showLog: Boolean = false
        ): AccessibilityNodeInfo? {
            val nodes = findAllNodesByText(parentNode, space, text, showLog)
            if (nodes.size > 0) {
                return nodes[0]
            }
            return null
        }

        fun findButtonNodeByText(
            parentNode: AccessibilityNodeInfo,
            space: Int,
            text: String?,
            showLog: Boolean = false
        ): AccessibilityNodeInfo? {
            val nodes = findAllNodesByText(parentNode, space, text, showLog)
            if (nodes.size > 0) {
                for (index in 0..nodes.size - 1) {
                    val node = nodes[index]
                    if (node.className == "android.widget.Button") {
                        return node
                    }
                }
            }
            return null
        }

        fun findAllNodesByText(
            parentNode: AccessibilityNodeInfo,
            space: Int,
            text: String? = "",
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
                    if (text != null && child.text != null && child.text.contains(text!!)) {
                        result.add(child)
                    }
                    if (child.childCount > 0) {
                        result.addAll(findAllNodesByText(child, space + 1, text, showLog))
                    }
                }
            }
            return result
        }

        fun findNodeByEqualsText(
            parentNode: AccessibilityNodeInfo,
            space: Int,
            text: String?,
            showLog: Boolean = false
        ): AccessibilityNodeInfo? {
            val nodes = findAllNodesByEqualsText(parentNode, space, text, showLog)
            if (nodes.size > 0) {
                return nodes[0]
            }
            return null
        }

        fun findAllNodesByEqualsText(
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
                    if (text != null && child.text != null && child.text.toString() == (text!!)) {
                        result.add(child)
                    }
                    if (child.childCount > 0) {
                        result.addAll(findAllNodesByEqualsText(child, space + 1, text, showLog))
                    }
                }
            }
            return result
        }

        fun findAllNodesByResId(
            parentNode: AccessibilityNodeInfo,
            space: Int,
            viewId: String?,
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
                    if (viewId != null && child.viewIdResourceName != null && child.viewIdResourceName == (viewId!!)) {
                        result.add(child)
                    }
                    if (child.childCount > 0) {
                        result.addAll(findAllNodesByResId(child, space + 1, viewId, showLog))
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
    }
}