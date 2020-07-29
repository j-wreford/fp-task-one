"use strict"

/**
 * Draws a maze on the available maze canvases
 */
const drawMazes = () => {

    const mazes = document.getElementsByClassName("maze")

    // bail if there are no mazes to draw on this page
    if (!mazes.length)
        return

    for (const maze of mazes) {

        // bail if this isn't actually a canvas
        if (maze.tagName !== "CANVAS")
            continue

        // setup route toggle if required
        if (maze.showRoute === undefined)
            maze.showRoute = true

        // parse maze data
        let config, passages, route

        try {
            config = JSON.parse(maze.getAttribute("data-config"))
            passages = JSON.parse(maze.getAttribute("data-passages"))
            route = JSON.parse(maze.getAttribute("data-route"))
        }
        catch (error) {
            console.warn("skipping maze (invalid configuration or passage data)", maze)
        }

        // grab size information of the canvas we're drawing to and update canvas
        const {width, height} = maze.getBoundingClientRect()
        maze.width = width
        maze.height = height

        // compute width and height of a cell
        const cellSize = {
            width: width / config.size.cols,
            height: height / config.size.rows,
        }

        // get our drawing context and render the maze
        const ctx = maze.getContext("2d")
        ctx.fillStyle = "#1C1F27"
        ctx.rect(0, 0, width, height)
        ctx.fill()

        // carve out the passages
        const cellBorderWidth = 2

        ctx.strokeStyle = "#303644"
        ctx.lineWidth = cellSize.width - (cellBorderWidth * 2)
        ctx.lineCap = "square"

        for (const [[rowA, colA], [rowB, colB]] of passages) {

            const pointA = {
                x: (colA * cellSize.width) + (cellSize.width / 2),
                y: (rowA * cellSize.height) + (cellSize.height / 2)
            }

            const pointB = {
                x: (colB * cellSize.width) + (cellSize.width / 2),
                y: (rowB * cellSize.height) + (cellSize.height / 2)
            }
    
            ctx.beginPath()
            ctx.moveTo(pointA.x, pointA.y)
            ctx.lineTo(pointB.x, pointB.y)
    
            ctx.stroke()
        }

        // draw route
        if (maze.showRoute && route) {

            ctx.strokeStyle = "#73C989"
            ctx.lineWidth = 4
            ctx.lineCap = "round"

            ctx.beginPath()

            let move = true
            for (const [row, col] of route) {

                const point = {
                    x: (col * cellSize.width) + (cellSize.width / 2),
                    y: (row * cellSize.height) + (cellSize.height / 2)
                }

                if (move) {
                    ctx.moveTo(point.x, point.y)
                    move = false
                } else {
                    ctx.lineTo(point.x, point.y)
                }
            }

            ctx.stroke()
        }
    }
}

window.addEventListener("load", drawMazes)
//window.addEventListener("resize", drawMazes)