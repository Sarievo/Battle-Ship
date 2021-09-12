import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        char[][] field1 = initField();
        char[][] field2 = initField();
        String[] prompt = { "Player 1, place your ships on the game field",
                        "Player 2, place your ships to the game field" };
        // Fetch player 1's field
        System.out.println(prompt[0]);
        int[][][] shipCoords1 = phraseI(s, field1);
        promptEnterKey();

        // Fetch player 2's field
        System.out.println(prompt[1]);
        int[][][] shipCoords2 = phraseI(s, field2);
        promptEnterKey();

        phraseII(s, field1, shipCoords1, field2, shipCoords2);
    }

    // int array of y1 x1 y2 x2
    static int[] getCoords(Scanner s, boolean isDuo) {
        String c1 = s.next();
        int y1t = c1.charAt(0) - 65;
        int x1t = c1.charAt(1) - 49;
        if (c1.length() == 3) x1t = c1.charAt(2) - 39;

        if (isDuo) {
            int[] c2 = getCoords(s, false);
            int y2t = c2[0], x2t = c2[1];

            int y1 = Math.min(y1t, y2t), y2 = Math.max(y1t, y2t);
            int x1 = Math.min(x1t, x2t), x2 = Math.max(x1t, x2t);
            return new int[]{y1, x1, y2, x2};
        }
        return new int[]{y1t, x1t};
    }

    static int[][][] phraseI(Scanner s, char[][] field) {
        int step = 0;
        List<Integer> shipLength = List.of(5, 4, 3, 3, 2);
        String[] prompt = {"Aircraft Carrier (5 cells)", "Battleship (4 cells)",
                "Submarine (3 cells)", "Cruiser (3 cells)", "Destroyer (2 cells)"};

        char[][] fieldBlc = initFieldCalcBlockage();
        printField(field);

        int[][][] shipC = initShipCoords();
        while (step < 5) {
            boolean state = true;

            System.out.printf("Enter the coordinates of the %s:\n", prompt[step]);
            while (state) {
                try {
                    int[] coords = getCoords(s, true);
                    int y1 = coords[0], x1 = coords[1], y2 = coords[2], x2 = coords[3];

                    if (x1 != x2 && y1 != y2) {
                        error(5);
                    } else if (x2 - x1 + 1 != shipLength.get(step)
                            && y2 - y1 + 1 != shipLength.get(step)) {
                        error(step);
                    } else {
                        if (!placeShipsLogCoords(field, fieldBlc, step, shipC, x1, x2, y1, y2)) {
                            error(6);
                            continue;
                        }
                        state = false;
                        step++;
                    }
                } catch (Exception e) {
                    error(5);
                }
            }
            printField(field);
        }
        return shipC;

    }

    static void phraseII(Scanner s, char[][] field1, int[][][] shipC1, char[][] field2, int[][][] shipC2) {
        char[][] field;
        int[][][] shipC;
        String[] prompt = {"Player 1, it's your turn:", "Player 2, it's your turn:", "---------------------"};

        char[][] fogOfWar1 = initFogOfWar();
        char[][] fogOfWar2 = initFogOfWar();
        char[][] fog;

        int playerTurn = 0;

        int[] shipsRemain1 = new int[5];
        shipsRemain1[0] = 5;
        shipsRemain1[1] = 4;
        shipsRemain1[2] = 3;
        shipsRemain1[3] = 3;
        shipsRemain1[4] = 2;
        int[] shipsRemain2 = shipsRemain1.clone();
        int[] shipsRemain;

        int totalRemain1 = 17;
        int totalRemain2 = 17;
        int totalRemain;

        while (true) {
            char[][] fieldP;
            if (playerTurn == 0) {
                field = field2;
                shipC = shipC2;
                fog = fogOfWar2;
                shipsRemain = shipsRemain2;
                totalRemain = totalRemain2;
                fieldP = field1;
            } else {
                field = field1;
                shipC = shipC1;
                fog = fogOfWar1;
                shipsRemain = shipsRemain1;
                totalRemain = totalRemain1;
                fieldP = field2;
            }

            printField(fog);
            System.out.println(prompt[2]);
            printField(fieldP);
            System.out.println(prompt[playerTurn]);

            try {
                int[] coords = getCoords(s, false);
                int y1 = coords[0], x1 = coords[1];

                if (field[y1][x1] == 'O') {
                    field[y1][x1] = 'X';
                    fog[y1][x1] = 'X';
                    int i = checkShipIndex(shipC, y1, x1);
                    shipsRemain[i]--;
                    totalRemain--;
                    if (totalRemain <= 0) {
                        System.out.println("You sank the last ship. You won. Congratulations!");
                        break;
                    }
                    if (shipsRemain[i] <= 0) {
                        System.out.println("You sank a ship!");
                    } else {
                        System.out.println("You hit a ship!");
                    }
                } else if (field[y1][x1] == '~') {
                    field[y1][x1] = 'M';
                    fog[y1][x1] = 'M';
                    System.out.println("You missed!");
                }
                promptEnterKey();
            } catch (Exception e) {
                error(7);
                continue;
            }

            if (playerTurn == 0) {
                shipsRemain2 = shipsRemain;
                totalRemain2 = totalRemain;
            } else {
                shipsRemain1 = shipsRemain;
                totalRemain1 = totalRemain;
            }

            if (playerTurn == 0) {
                playerTurn = 1;
            } else {
                playerTurn = 0;
            }
        }
    }


    static boolean placeShipsLogCoords(char[][] field, char[][] fieldBlc, int step, int[][][] shipC,
                                       int x1, int x2, int y1, int y2) {
        // check blockage
        for (int i = y1; i <= y2; i++) {
            for (int j = x1; j <= x2; j++) {
                if (fieldBlc[i + 1][j + 1] == 'B') return false;
            }
        }

        // place ships
        int currentPlace = 0;
        for (int i = y1; i <= y2; i++) {
            for (int j = x1; j <= x2; j++) {
                field[i][j] = 'O';
                shipC[step][currentPlace][0] = i;
                shipC[step][currentPlace][1] = j;
                currentPlace++;
            }
        }

        // set blockage
        int x2t = x2 + 2, y2t = y2 + 2;
        for (int i = y1; i <= y2t; i++) {
            for (int j = x1; j <= x2t; j++) {
                fieldBlc[i][j] = 'B';
            }
        }
        return true;
    }

    static char[][] initField() {
        char[][] field = new char[10][10];
        for (char[] row : field)
            Arrays.fill(row, '~');
        return field;
    }

    static char[][] initFieldCalcBlockage() {
        char[][] fieldB = new char[12][12];
        for (char[] row : fieldB)
            Arrays.fill(row, '~');
        return fieldB;
    }

    static char[][] initFogOfWar() {
        char[][] fieldF = new char[10][10];
        for (char[] row : fieldF)
            Arrays.fill(row, '~');
        return fieldF;
    }

    static int[][][] initShipCoords() {
        int[][][] shipC = new int[5][][];
        shipC[0] = new int[5][2];
        shipC[1] = new int[4][2];
        shipC[2] = new int[3][2];
        shipC[3] = new int[3][2];
        shipC[4] = new int[2][2];
        return shipC;
    }

    static int checkShipIndex(int[][][] shipC, int y, int x) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < shipC[i].length; j++) {
                if (shipC[i][j][0] == y && shipC[i][j][1] == x) {
                    return i;
                }
            }
        }
        return -1;
    }

    static void printField(char[][] field) {
        char charIndex = 'A';
        System.out.println("  1 2 3 4 5 6 7 8 9 10");
        for (char[] row : field) {
            System.out.print(charIndex + " ");
            charIndex++;
            for (char i : row) {
                System.out.print(i + " ");
            }
            System.out.println();
        }
    }

    static void error(int i) {
        String[] prompt = {"Aircraft Carrier", "Battleship", "Submarine", "Cruiser", "Destroyer"};
        String msg = switch (i) {
            case 5 -> "Wrong ship location!";
            case 6 -> "Your placed it too close to another one.";
            case 7 -> "You entered the wrong coordinates!";
            default -> "Wrong length of the " + prompt[i] + "!";
        };
        System.out.printf("Error! %s Try again:\n", msg);
    }

    public static void promptEnterKey() {
        System.out.println("Press Enter and pass the move to another player");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
