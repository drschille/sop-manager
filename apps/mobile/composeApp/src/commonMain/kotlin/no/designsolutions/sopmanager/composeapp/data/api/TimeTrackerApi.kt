package no.designsolutions.timetracker.composeapp.data.api

import com.kansson.kmp.convex.core.ConvexFunction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object TimeTrackerApi {
    object WorkStations {
        data class ListWorkStations(
            override val identifier: String = "workStations:listWorkStations",
            override val args: Unit = Unit,
        ) : ConvexFunction.Query<Unit, List<WorkStationSummary>>

        data class GetWorkStationBoard(
            override val identifier: String = "workStations:getWorkStationBoard",
            override val args: Args,
        ) : ConvexFunction.Query<GetWorkStationBoard.Args, WorkStationBoard?> {
            @Serializable
            data class Args(
                val workStationId: String,
            )
        }
    }

    object Runs {
        data class StartTask(
            override val identifier: String = "runs:startTask",
            override val args: Args,
        ) : ConvexFunction.Mutation<StartTask.Args, String?> {
            @Serializable
            data class Args(
                val runId: String,
                val runTaskId: String,
            )
        }

        data class CompleteTask(
            override val identifier: String = "runs:completeTask",
            override val args: Args,
        ) : ConvexFunction.Mutation<CompleteTask.Args, String?> {
            @Serializable
            data class Args(
                val runId: String,
                val runTaskId: String,
            )
        }

        data class RollbackTaskStart(
            override val identifier: String = "runs:rollbackTaskStart",
            override val args: Args,
        ) : ConvexFunction.Mutation<RollbackTaskStart.Args, String?> {
            @Serializable
            data class Args(
                val runId: String,
                val runTaskId: String,
            )
        }
    }
}

@Serializable
data class WorkStationSummary(
    @SerialName("_id")
    val id: String,
    val name: String,
    val color: String? = null,
    val isActive: Boolean = true,
)

@Serializable
data class WorkStationBoard(
    val station: WorkStationSummary,
    val ongoing: List<WorkQueueItem>,
    val ready: List<WorkQueueItem>,
    val upcoming: List<WorkQueueItem>,
)

@Serializable
data class WorkQueueItem(
    val runId: String,
    val runStatus: String,
    val runStartedAt: Double,
    val routeId: String,
    val routeName: String,
    val runTaskId: String,
    val taskName: String,
    val taskOrder: Double,
    val taskStatus: String,
    val taskStartedAt: Double? = null,
    val tasksBeforeRemaining: Double,
    val routeFlowState: String,
)
