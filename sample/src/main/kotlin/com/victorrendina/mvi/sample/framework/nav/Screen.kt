package com.victorrendina.mvi.sample.framework.nav

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.AnimRes
import androidx.fragment.app.Fragment
import com.victorrendina.mvi.Mvi
import com.victorrendina.mvi.MviArgs
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.framework.BaseFragmentSlidingActivity
import kotlinx.android.parcel.Parcelize
import java.lang.IllegalArgumentException

/**
 * Data class used to navigate between screens within the application. The constructor should not be used directly,
 * instead use a ScreenBuilder from Java or the [screen] extension function from Kotlin.
 *
 * For example:
 * ```
 * nav.pushScreen(screen(MyFragment::class.java) {
 *      arguments = MyArgs(true)
 * })
 *```
 *
 */

@Parcelize
data class Screen(
    val fragment: String,
    val arguments: MviArgs?,
    @AnimRes val enterAnimation: Int,
    @AnimRes val exitAnimation: Int,
    @AnimRes val popEnterAnimation: Int,
    @AnimRes val popExitAnimation: Int,
    val addToBackStack: Boolean,
    val backStackTag: String,
    val activity: String?
) : Parcelable {

    /**
     * Starts a new activity from this screen definition.
     *
     * @param context Context to start the activity from. If the context is not an activity the FLAG_ACTIVITY_NEW_TASK
     * will be added automatically.
     */
    fun startActivity(context: Context) {
        context.startActivity(buildIntent(context))
    }

    /**
     * Start a new activity from the screen definition for a result with the target fragment.
     *
     * @param target fragment that will receive the activity result
     * @param requestCode code to track this request
     */
    fun startActivityForResult(target: Fragment, requestCode: Int) {
        target.startActivityForResult(buildIntent(target.requireActivity()), requestCode)
    }

    private fun buildIntent(context: Context): Intent {
        if (activity != null) {
            return Intent(context, Class.forName(activity)).apply {
                putExtra(KEY_SCREEN, this@Screen)
                if (arguments != null) {
                    putExtra(Mvi.KEY_ARG, arguments)
                }
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
        }
        throw IllegalArgumentException("Attempted to start activity without activity set in screen")
    }

    /**
     * Create an instance of the fragment in this screen definition with all the appropriate arguments attached.
     */
    fun createFragment(): Fragment {
        val fragment = Class.forName(fragment).newInstance() as Fragment

        fragment.arguments = Bundle().apply {
            putParcelable(KEY_SCREEN, this@Screen)
            if (arguments != null) {
                putParcelable(Mvi.KEY_ARG, arguments)
            }
        }
        return fragment
    }

    companion object {
        const val KEY_SCREEN = "screen"
    }
}

class ScreenBuilder(val fragment: Class<out Fragment>) {

    var arguments: MviArgs? = null
    var addToBackStack: Boolean = true
    var backStackTag: String = fragment.simpleName

    var activity: Class<out Activity>? = null
        set(value) {
            field = value
            // If the activity is a sliding activity then change the default animations
            if (value != null && BaseFragmentSlidingActivity::class.java.isAssignableFrom(value)) {
                enterAnimation = R.anim.slide_in_bottom_fast
                popExitAnimation = R.anim.slide_out_bottom_fast
            }
        }

    @AnimRes
    var enterAnimation: Int = R.anim.slide_in_right
    @AnimRes
    var exitAnimation: Int = R.anim.slide_out_left
    @AnimRes
    var popEnterAnimation: Int = R.anim.slide_in_left
    @AnimRes
    var popExitAnimation: Int = R.anim.slide_out_right

    /**
     * Arguments that will be supplied to the fragment and view models under the key [Mvi.KEY_ARG].
     */
    fun withArguments(arguments: MviArgs): ScreenBuilder {
        this.arguments = arguments
        return this
    }

    @JvmOverloads
    fun withAnimations(@AnimRes enterAnimation: Int = 0, @AnimRes exitAnimation: Int = 0, @AnimRes popEnterAnimation: Int = 0, @AnimRes popExitAnimation: Int = 0): ScreenBuilder {
        this.enterAnimation = enterAnimation
        this.exitAnimation = exitAnimation
        this.popEnterAnimation = popEnterAnimation
        this.popExitAnimation = popExitAnimation
        return this
    }

    /**
     * Sets a custom back stack tag for the fragment. By default the back stack tag will be equal to the class simple
     * name which can be obtained from Fragment::class.java.simpleName.
     */
    fun withBackStackTag(backStackTag: String): ScreenBuilder {
        this.backStackTag = backStackTag
        return this
    }

    /**
     * If the fragment should be added to the back stack. This defaults to true and should generally not be changed.
     */
    fun addToBackStack(addToBackStack: Boolean): ScreenBuilder {
        this.addToBackStack = addToBackStack
        return this
    }

    /**
     * Set an activity to use for this screen. If an activity is set then a new activity should always be opened unless
     * the type of activity only allows for a single instance.
     */
    fun withActivity(activity: Class<out Activity>): ScreenBuilder {
        this.activity = activity
        return this
    }

    fun build(): Screen {
        return Screen(
            fragment = fragment.name,
            arguments = arguments,
            addToBackStack = addToBackStack,
            enterAnimation = enterAnimation,
            exitAnimation = exitAnimation,
            popEnterAnimation = popEnterAnimation,
            popExitAnimation = popExitAnimation,
            backStackTag = backStackTag,
            activity = activity?.name
        )
    }
}

fun screen(fragment: Class<out Fragment>, init: (ScreenBuilder.() -> Unit)? = null): Screen {
    val builder = ScreenBuilder(fragment)
    if (init != null) {
        builder.init()
    }
    return builder.build()
}

fun Intent.getScreen(): Screen? = extras?.getScreen()

fun Bundle.getScreen(): Screen? = getParcelable(Screen.KEY_SCREEN)