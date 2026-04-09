# Task: Fix Manual Entry TextField Behavior

## 1. Context
On the Validation/Manual Entry screen, the user is experiencing two issues:
1. The TextField allows letters, but it should strictly accept numbers (and optionally a decimal point).
2. Typing a character in the TextField immediately navigates the user back to the Camera screen. This indicates a recomposition bug where updating the text state accidentally triggers a navigation side-effect.

## 2. Instructions for AI

> @workspace Please fix the `TextField` (or `OutlinedTextField`) on the Validation/Manual Entry screen:
>
> 1. **Restrict Input to Numbers:**
>    - Add `keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberDecimal)` to the TextField.
>    - Inside the `onValueChange` lambda, filter the incoming string to only allow digits (and a maximum of one decimal point). Update the state only with the filtered string.
>
> 2. **Fix Recomposition Navigation Bug:**
>    - Investigate why updating the text state triggers navigation. 
>    - Check for improperly placed navigation calls (`popBackStack`, `onRetake()`, `onCancel()`) that might be executing directly in the composable body or inside `onValueChange` instead of inside an `onClick` listener.
>    - Check `LaunchedEffect` blocks. If navigation depends on a UI state, ensure updating the text field doesn't inadvertently trigger the exit condition.
>    - Ensure navigation callbacks are strictly bound to the "Cancel" and "Save Reading" buttons.