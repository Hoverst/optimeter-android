# Task: Fix Dropdown Menu Width & Arrow Placement

## 1. Current Bug

In the Dashboard screen, the home selector dropdown menu shrinks to the width of the currently selected item. If the selected home is "77", a longer home like "657567576" gets wrapped into multiple lines inside the dropdown menu.

## 2. Requirements

1. **Unconstrained Menu Width:** The `DropdownMenu` itself must always be at least as wide as its longest item, regardless of the width of the trigger/button.
2. **Arrow Placement:** On the main screen (the trigger button), the text should be on the left, and the dropdown arrow (`Icon`) should be strictly on the right side.
3. **No Text Wrapping:** Dropdown items should NEVER wrap to a new line (`maxLines = 1`, `overflow = TextOverflow.Ellipsis`).

## 3. Implementation Instructions for AI

Please rewrite the `HomeSelector` component (in `HomeTab.kt` or `DashboardScreen.kt`).

**Follow this approach using a standard `Box` and `DropdownMenu` (not `ExposedDropdownMenuBox` if it's causing constraints):**

```kotlin
var expanded by remember { mutableStateOf(false) }

Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
    // 1. The Trigger Button
    Row(
        modifier = Modifier
            .clickable { expanded = true }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = selectedHomeName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false) // Allows text to take space but not push arrow off-screen
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "Expand",
            // Optional: Animate arrow rotation based on 'expanded' state
        )
    }

    // 2. The Dropdown Menu
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        // Crucial: Let the menu size itself based on its content, not the parent Row
        modifier = Modifier.wrapContentWidth()
    ) {
        homes.forEach { home ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = home.name,
                        maxLines = 1, // Prevent wrapping
                        softWrap = false
                    )
                },
                onClick = {
                    onHomeSelected(home)
                    expanded = false
                }
            )
        }
    }
}
```
