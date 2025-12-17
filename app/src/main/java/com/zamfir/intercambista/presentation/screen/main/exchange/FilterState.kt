package com.zamfir.intercambista.presentation.screen.main.exchange

data class FilterState (
    val filter : Filter = Filter()
)

data class Filter(
    val byName : Stage = Stage.UNSELECTED,
    val byCode : Stage = Stage.UNSELECTED,
    val byValue : Stage = Stage.UNSELECTED
)

enum class Stage(val value : Int){
    UNSELECTED(0),
    ASC(1),
    DESC(2);

    companion object{
        fun nextStage(actualState : Stage) : Stage{
            val increment = actualState.value + 1

            return when{
                increment > 2 -> { UNSELECTED }
                increment == 1 -> { ASC }
                increment == 2 -> { DESC }
                else -> UNSELECTED
            }
        }

        fun getStateByVal(value : Int) : Stage{
            return when(value){
                0 -> UNSELECTED
                1 -> ASC
                2 -> DESC
                else -> UNSELECTED
            }
        }
    }
}