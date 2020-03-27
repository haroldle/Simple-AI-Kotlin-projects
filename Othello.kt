import kotlin.collections.ArrayList
import kotlin.random.Random
import javax.swing.*
import java.awt.*
import java.lang.Exception
import kotlin.math.max
import kotlin.math.min

class Othello{
    //TURN -1 IS O OR BLACK, TURN 1 IS X OR WHITE
    private val inf = 999999999
    private val negINF = -999999999
    private var SPEED = 100
    private var state: Array<Byte> = Array<Byte>(100) {0}
    private var BLOCK:Int = 40
    private val dirs = arrayOf(Pair(0,-1),Pair(0,1),Pair(-1,-1),Pair(-1,0),Pair(-1,1),Pair(1,-1),Pair(1,0),Pair(1,1))
    private lateinit var OthelloComp: Component
    private var xSkips = false
    private var oSkips = false
    init {
        state[44] = 1
        state[45] = -1
        state[54] = -1
        state[55] = 1
    }
    //https://guides.net4tv.com/games/how-win-reversi reference
    //better heuristic function: sides of board and conners weight more points than placing and capture turing pieces on the board.
    //penalty: is higher than the reward. The expectation: the both AI players will try to play the game somewhat in the center.
   /* private fun evaluate(id:Byte):Int{
        val conners = arrayOf(0,9,90,99)
        val deathTile = arrayOf(1,10,11,8,18,19,80,81,91,88,89,98)
        //var value = state.count{it==id}
        var value = 5000
        for(i in deathTile)
                when(state[i]){
                    id->value -= inf
                    (-id).toByte()->value += inf
                    else->value
                }
        for(conner in conners)
            when(state[conner]){
                id->value+=inf
                (-id).toByte()->value-=5000
                else->value
            }
       value -= validmoves((-id).toByte()).size * 5
        return value
    }
    */
// have not test this heu
    private fun evaluate(id:Byte):Int{
        val conners = arrayOf(0,9,90,99)
        val secondPrior = arrayOf(2,7,92,97,29,20,70,79)
        val thirdPrior = arrayOf(63,66,23,26)
        val deathTile = arrayOf(1,10,11,8,18,19,80,81,91,88,89,98)
        //var value = state.count{it==id}
        var value = 5000
        for(i in secondPrior)
            if(state[i]==id)
                value += 2500
            else
                value -= 2500
        for(i in thirdPrior)
            if(state[i]==id)
                value += 750
            else
                value -= 750
        for(i in deathTile)
            when(state[i]){
                id->value -= 50000
                (-id).toByte()->value += 50000
                else->value
            }
        for(conner in conners)
            when(state[conner]){
                id->value+=75000
                (-id).toByte()->value-=100000
                else->value
            }
        //value -= validmoves((-id).toByte()).size * 5
        return value
    }

    //simple heuristic just count all of the pieces on the board.
    private fun simpleHeu(id:Byte):Int{
        return state.count{it == id}
    }

    private fun win(id:Byte):Boolean{
        val playerPiece = state.count{(it)==id}
        val opponentPiece = state.count{(it)==(-1*id).toByte()}
        val emptyTile = state.count{(it).toInt()==0}
        return (100 - playerPiece - emptyTile > opponentPiece) && (end(id)||(xSkips&&oSkips))
    }

    private fun copy(): Othello{
        val board = Othello()
        board.state = state.copyOf()
        return board
    }

    private fun index(x:Int,y:Int):Int{
        if(x in 0..9 && y in 0..9){
            return (state[x+y*10]).toInt()
        }else{
            return -2
        }
    }

    private fun canplace(x:Int,y:Int,id:Byte):Boolean{
        if(index(x,y)!=0)
          return false
        for(dir in 0..7){
            var i = x + dirs[dir].first
            var j = y + dirs[dir].second
            if (index(i,j)!=-id)
                continue
            i += dirs[dir].first
            j += dirs[dir].second
            while(index(i,j)==-id){
                i += dirs[dir].first
                j += dirs[dir].second
            }
            if(index(i,j)==id.toInt())
                return true
        }
        return false
    }

    private fun place(x:Int,y:Int,id:Byte){
        if(!canplace(x,y,id))
            return
        state[x+y*10]=id
        for(dir in 0..7){
            var i = x + dirs[dir].first
            var j = y + dirs[dir].second
            if(index(i,j)!=-id)
                continue
            while(index(i,j)==-id){
                i += dirs[dir].first
                j += dirs[dir].second
            }
            if(index(i,j)==id.toInt()){
                var k = x + dirs[dir].first
                var l = y + dirs[dir].second
                while(k!=i || l!=j){
                    state[k+l*10]=id
                    k += dirs[dir].first
                    l += dirs[dir].second
                }
            }
        }
    }

    private fun validmoves(id:Byte):ArrayList<Pair<Int,Int>>{
        var moves:ArrayList<Pair<Int, Int>> = ArrayList()
        for(x in 0 until 10){
            for(y in 0 until 10){
                if(canplace(x,y,id)){
                    moves.add(Pair(x,y))
                }
            }
        }
    return moves
    }

    private fun printboard(){
        for(y in 0 until 10){
            var line =""
            for(x in 0 until 10){
                if(index(x,y)==1){
                    line=line+"X"
                }
                else if(index(x,y)==-1){
                    line=line+"O"
                }
                else{
                    line = line+"."
                }
            }
            println(line)
        }
    }

    private fun end(id:Byte):Boolean{
        return state.indexOf(0)==-1 || win(id)
    }

    private fun countO():Int{
        return state.count{it.toInt() == -1}
    }

    private fun countEmpty():Int{
        return state.count{it.toInt()==0}
    }

    private fun declare(){
        if(countO()>100-countO()-countEmpty())
            println("BLACK Wins the game ${countO()}")
        else if(countO()<100-countO()-countEmpty())
            println("WHITE Wins the game ${100-countO()-countEmpty()}")
        else
            println("DRAW")
    }
    private fun CreateAGUIBoard(){
        val f: JFrame = JFrame()
        f.title = "OTHELLO"
        f.setSize(BLOCK*11, BLOCK*12)
        f.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        OthelloComp = Component()
        f.add(OthelloComp)
        f.isVisible = true
    }
    //Greedy Strategy: Only choose the maximum number of pieces on the Board.
    fun gameOnChooseTheBest(){
        var board = Othello()
        var turn=1
        //Initialize the board
        CreateAGUIBoard()
        //Check if player skips the moves
        var xSkips = false
        var oSkips = false

        while(true){
            var bestSoFar = 0
            var considerMove:Int
            var keepPair:Int = 0
            val movelist = board.validmoves(turn.toByte())
            //if board is filled or two players cannot place the moves
            if(end(turn.toByte())||(xSkips&&oSkips))
                break
            //check who skip the move
            if(movelist.size==0)
                if(turn==-1)
                    oSkips = true
                else
                    xSkips = true
            //player make the move
            if(movelist.size>0)
            {
                //in case the previous move from the previous player skip
                if(turn==-1)
                    xSkips=false
                else
                    oSkips=false
                //check every moves
                for(i in 0 until movelist.size){
                    var tempBoard = board.copy()
                    tempBoard.place(movelist[i].first, movelist[i].second,turn.toByte())
                    considerMove = tempBoard.evaluate(turn.toByte())
                    if(bestSoFar<considerMove){
                            bestSoFar = considerMove
                            keepPair = i
                        }
                }
                board.place(movelist[keepPair].first, movelist[keepPair].second,turn.toByte())
                state = board.state
                OthelloComp.repaint()
            }
            turn=-turn
            try{
                Thread.sleep(SPEED.toLong())
            } catch (e: Exception){ }
        }
        //find out who wins the game black or white
        declare()
    }

    //MINIMAX DEPTH OF 2
    //Evaluation in this place how many black are there compares to how many whites
    fun gameOnMinimaxCustom(black:Int,white:Int,random:Boolean){
        var turn=1
        CreateAGUIBoard()
        while(true){
            if(end(turn.toByte())||(xSkips&&oSkips))
                break
            if(turn==1) {
                if(!random)
                    miniMaxHelper(white*2, turn)
                else{
                    val moveListConsider = validmoves(turn.toByte())
                    if(moveListConsider.size>0) {
                        xSkips = false
                        val keepPair = Random.nextInt(0, moveListConsider.size)
                        place(moveListConsider[keepPair].first, moveListConsider[keepPair].second,turn.toByte())
                    }else
                        oSkips = true
                }
            }else
                miniMaxHelper(black*2,turn)

            OthelloComp.repaint()
            turn =-turn
            try{
                Thread.sleep(SPEED.toLong())
            } catch (e: Exception){ }
        }
        declare()
    }

    fun gameOnAlBe(black:Int,white:Int,random:Boolean){
        var turn=1
        CreateAGUIBoard()
        while(true){
            if(end(turn.toByte())||(xSkips&&oSkips))
                break
                if(turn==1) {
                    if(!random)
                        ABPrunningHelper(white*2, turn)
                    else{
                        val moveListConsider = validmoves(turn.toByte())
                        if(moveListConsider.size>0) {
                            xSkips = false
                            val keepPair = Random.nextInt(0, moveListConsider.size)
                            place(moveListConsider[keepPair].first, moveListConsider[keepPair].second,turn.toByte())
                        }else
                            oSkips = true
                    }
                }else
                    ABPrunningHelper(black*2,turn)
            OthelloComp.repaint()
            turn =-turn
            try{
                Thread.sleep(SPEED.toLong())
            } catch (e: Exception){ }
        }
        declare()
    }

    fun ABPrunningHelper(Depth: Int,turn:Int){
        val playerValidMoves = validmoves(turn.toByte())
        var keepMove: Pair<Int, Int> = Pair(-90,-90)
        var scoring = negINF
        var tempHolder = 0
        for (move in playerValidMoves) {
            val tempBoard = state.copyOf()
            place(move.first,move.second,turn.toByte())
            tempHolder = AlPhaBeta(Depth, turn.toByte(), (-turn).toByte(), negINF,inf,true)
            if (scoring <= tempHolder) {
                scoring = tempHolder
                keepMove = move
                if (turn == -1)
                    xSkips = false
                else
                    oSkips = false
            }
            state = tempBoard.copyOf()
        }
        if(playerValidMoves.size==0) {
            if (turn == -1)
                oSkips = true
            else
                xSkips = true
            return
        }
        place(keepMove.first,keepMove.second,turn.toByte())
    }

    fun AlPhaBeta(Depth: Int,theActualPlayerConsider:Byte,opponent:Byte,alpha:Int, beta:Int,IsMini:Boolean):Int{
        if(Depth==0) {
            return evaluate(theActualPlayerConsider)
        }
        else if(end(theActualPlayerConsider))
        {
            return if(IsMini && win(theActualPlayerConsider)||win(theActualPlayerConsider))
                50000
            else
                -50000
        }
        else if(IsMini){
            var WorstScore = beta //inf
            val allWorstMoves = validmoves(opponent)
            for(move in allWorstMoves){
                val minLayerBoard = state.copyOf()
                place(move.first,move.second,opponent)
                WorstScore = min(WorstScore,AlPhaBeta(Depth-1,theActualPlayerConsider,opponent,alpha,WorstScore,false))
                state = minLayerBoard.copyOf()
                if(WorstScore<=alpha)
                    break
            }
            return WorstScore
        }
        else{
            var BestScore = alpha //negINF
            val allBestMove = validmoves(theActualPlayerConsider)
            for(move in allBestMove){
                val maxLayerBoard = state.copyOf()
                place(move.first,move.second,theActualPlayerConsider)
                BestScore = max(BestScore, AlPhaBeta(Depth-1,theActualPlayerConsider,opponent,BestScore,beta,true))
                state = maxLayerBoard.copyOf()
                if(beta<=BestScore)
                    break
            }
            return BestScore
        }
    }

    fun miniMaxHelper(Depth: Int,turn:Int){
    val playerValidMoves = validmoves(turn.toByte())
    var keepMove: Pair<Int, Int> = Pair(-90,-90)
    var scoring = negINF
    var tempHolder = 0
    for (move in playerValidMoves) {
        val tempBoard = state.copyOf()
        place(move.first,move.second,turn.toByte())
        tempHolder = minimax(Depth, turn.toByte(), (-turn).toByte(),true)
        if (scoring < tempHolder) {
            scoring = tempHolder
            keepMove = move
            if (turn == -1)
                xSkips = false
            else
                oSkips = false

        }
        state = tempBoard.copyOf()
    }
    if(playerValidMoves.size==0) {
        if (turn == -1)
            oSkips = true
        else
            xSkips = true
        return
    }
    place(keepMove.first,keepMove.second,turn.toByte())
    }

    fun minimax(Depth: Int,theActualPlayerConsider:Byte,opponent:Byte,IsMini:Boolean):Int{
        if(Depth==0) {
            return evaluate(theActualPlayerConsider)
        }
        else if(end(theActualPlayerConsider))
        {
            return if(IsMini && win(theActualPlayerConsider)||win(theActualPlayerConsider))
                50000
            else
                -50000
        }
        else if(IsMini){
            var WorstScore = inf
            val allWorstMoves = validmoves(opponent)
            for(move in allWorstMoves){
                val minLayerBoard = state.copyOf()
                place(move.first,move.second,opponent)
                WorstScore = min(WorstScore,minimax(Depth-1,theActualPlayerConsider,opponent,false))
                state = minLayerBoard.copyOf()
            }
            return WorstScore
        }
        else{
            var BestScore = negINF
            val allBestMove = validmoves(theActualPlayerConsider)
            for(move in allBestMove){
                val maxLayerBoard = state.copyOf()
                place(move.first,move.second,theActualPlayerConsider)
                BestScore = max(BestScore, minimax(Depth-1,theActualPlayerConsider,opponent,true))
                state = maxLayerBoard.copyOf()
            }
            return BestScore
        }
    }

    inner class Component:JComponent(){
        override fun paintComponent(g: Graphics?) {
            super.paintComponent(g)
            g?.color = Color.ORANGE
            g?.fillRect(0,0,BLOCK*10,BLOCK*10)
            for(row in 0..BLOCK*9 step BLOCK)
                for (col in ((row/BLOCK)%2)*BLOCK..BLOCK*9 step BLOCK*2) {
                    g?.color = Color.CYAN
                    g?.fillRect(col, row, BLOCK, BLOCK)
                }
                for(y in 0 until 10){
                    for(x in 0 until 10){
                        if(index(x,y)==1){
                            g?.color = Color.WHITE
                            g?.fillOval(x*BLOCK,y*BLOCK,BLOCK,BLOCK)
                        }
                        else if(index(x,y)==-1){
                            g?.color = Color.BLACK
                            g?.fillOval(x*BLOCK,y*BLOCK,BLOCK,BLOCK)
                        }
                    }
                }
        }
    }
}
