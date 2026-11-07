package com.fheicode.mofit.data

/** Starter content for the very first launch, so the app isn't empty. Everything can be edited or deleted. */
object SampleData {
    fun create(): AppData {
        val bench = Exercise(name = "Bankdrücken", muscleGroup = "Brust", emoji = "🏋️", colorIndex = 0)
        val squat = Exercise(name = "Kniebeugen", muscleGroup = "Beine", emoji = "🦵", colorIndex = 1)
        val deadlift = Exercise(name = "Kreuzheben", muscleGroup = "Rücken", emoji = "🔥", colorIndex = 3)
        val pullup = Exercise(name = "Klimmzüge", muscleGroup = "Rücken", emoji = "🧗", colorIndex = 2, bodyweight = true)
        val pushup = Exercise(name = "Liegestütze", muscleGroup = "Brust", emoji = "💪", colorIndex = 4, bodyweight = true)
        val press = Exercise(name = "Schulterdrücken", muscleGroup = "Schultern", emoji = "🚀", colorIndex = 5)
        val curls = Exercise(name = "Bizeps-Curls", muscleGroup = "Bizeps", emoji = "💪", colorIndex = 6)
        val crunches = Exercise(name = "Crunches", muscleGroup = "Bauch", emoji = "⚡", colorIndex = 7, bodyweight = true)

        val plan = listOf(
            PlanItem(dayOfWeek = 1, exerciseId = bench.id, sets = 3, weightKg = 30.0, order = 0),
            PlanItem(dayOfWeek = 1, exerciseId = press.id, sets = 3, weightKg = 10.0, order = 1),
            PlanItem(dayOfWeek = 1, exerciseId = pushup.id, sets = 3, weightKg = 0.0, order = 2),
            PlanItem(dayOfWeek = 3, exerciseId = squat.id, sets = 3, weightKg = 30.0, order = 0),
            PlanItem(dayOfWeek = 3, exerciseId = deadlift.id, sets = 3, weightKg = 40.0, order = 1),
            PlanItem(dayOfWeek = 3, exerciseId = crunches.id, sets = 3, weightKg = 0.0, order = 2),
            PlanItem(dayOfWeek = 5, exerciseId = pullup.id, sets = 3, weightKg = 0.0, order = 0),
            PlanItem(dayOfWeek = 5, exerciseId = curls.id, sets = 3, weightKg = 8.0, order = 1),
            PlanItem(dayOfWeek = 5, exerciseId = bench.id, sets = 3, weightKg = 32.5, order = 2),
        )

        return AppData(
            exercises = listOf(bench, squat, deadlift, pullup, pushup, press, curls, crunches),
            plan = plan,
        )
    }
}
