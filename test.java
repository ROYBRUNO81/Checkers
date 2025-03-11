package org.cis1200.checkers;

public class test {
    /**
     * Code to print elemets of a string two dimensional array in a grid
     */
    public static void main(String[] args) {
        String[][] grid = {{"What?", "Who?"}, {"What?", "Who?"}, {"What?", "Who?"}, {"What?", "Who?"}};
        for (int i = 0; i < grid.length; i++) {
            System.out.println();
            for (int j = 0; j < grid[i].length; j++) {
                System.out.print(" ");
                System.out.print(grid[i][j]);
            }
        }
    }
}
