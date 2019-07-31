/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pingrex.preview.photoview.scrollerproxy

import android.content.Context

abstract class ScrollerProxy {

    abstract val isFinished: Boolean

    abstract val currX: Int

    abstract val currY: Int

    abstract fun computeScrollOffset(): Boolean

    abstract fun fling(startX: Int, startY: Int, velocityX: Int, velocityY: Int, minX: Int, maxX: Int, minY: Int,
                       maxY: Int, overX: Int, overY: Int)

    abstract fun forceFinished(finished: Boolean)

    companion object {

        fun getScroller(context: Context): ScrollerProxy {
            return IcsScroller(context)
        }
    }


}
