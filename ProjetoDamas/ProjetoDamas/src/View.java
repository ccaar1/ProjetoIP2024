import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import pt.iscte.guitoo.Color;
import pt.iscte.guitoo.StandardColor;
import pt.iscte.guitoo.board.Board;

public class View {
	Board board;
	int[][] piecePositions;
	Position selectedPiece = new Position(0,0);
	Position lastselectedPiece = new Position(0,0); // to store in memory selected piece before current selection
	View gui;
	int turn = 2; // 2 means white go first
	boolean finished = false;
	
	View(int size) {
		board = new Board("...", size, size,50); //tem de ser flexivel de forma a que o jogador possa impor dimensoes
		piecePositions = new int[size][size];
        initializePieces(size);
		board.addAction("aleatorio",this::random);
		board.addAction("novo", this::action); //adiciona botao
		board.addAction("gravar", () -> save("game"));
		board.addAction("carregar", () -> load("game"));
		board.setBackgroundProvider(this::background); //sets background
		board.setIconProvider(this::icon);
		board.addMouseListener(this::click);
		
	}

	void start() {
		board.open();
	}
	
	record Position(int line, int col) {
		
		public Position(int line, int col) {
			this.line = line;
			this.col = col;
		}

		
		boolean isAdjacent(Position p) {
			return Math.abs(line - p.line()) + Math.abs(col - p.col()) == 1;
		}
		
		 boolean isJumpValid(Position middle, Position target) {
		        return Math.abs(line - target.line()) == 2 && Math.abs(col - target.col()) == 2 &&
		                middle.line() == (line + target.line()) / 2 &&
		                middle.col() == (col + target.col()) / 2;
		    }
		 
		    public int getLine() {
		        return line;
		    }

		    public int getCol() {
		        return col;
		    }
	}
	
	void initializePieces(int size) {
        for (int row = 0; row < size / 2 - 1; row++) { //da esquerda para a direita
            for (int col = 0; col < size; col++) {
                if ((row + col) % 2 != 0) {
                    piecePositions[row][col] = 1; // pecas pretas
                }
            }
        }
        for (int row = size - (size / 2 - 1); row < size; row++) { // a partir de baixo, da direita para a esquerda
            for (int col = 0; col < size; col++) {
                if ((row + col) % 2 != 0) {
                    piecePositions[row][col] = 2; // pecas brancas
                }
            }
        }
    }

	void action() {
		int tamanho = board.promptInt("Tamanho?"); //pergunta tamanho do board
		if (tamanho >= 0) {
			View gui = new View(tamanho);
			gui.start();
		}
	}

	

		
	void random() {
	    boolean moveMade = false; // Flag to indicate if a move was made
	    int boardSize = piecePositions.length; // Assuming square board for simplicity

	    while (!moveMade) {
	        // find a piece of the current player
	        for (int i = 0; i < boardSize; i++) {
	            for (int j = 0; j < boardSize; j++) {
	                // Check if the piece belongs to the current player
	                if (piecePositions[i][j] == turn) {
	                    Position lastSelected = new Position(i, j);

	                    // Try random moves for this piece
	                    for (int attempts = 0; attempts < 10; attempts++) {
	                        int randomMoveX = (turn == 2) ? -1 : 1; // White moves up, Black moves down
	                        int randomMoveY = (Math.random() < 0.5) ? -1 : 1; // Random left or right

	                        int newRow = i + randomMoveX;
	                        int newCol = j + randomMoveY;

	                        // Check if the new position is within bounds
	                        if (newRow >= 0 && newRow < boardSize && newCol >= 0 && newCol < boardSize) {
	                            Position selected = new Position(newRow, newCol);

	                            // Validate the move
	                            if (isValidMove(selected, lastSelected, turn)) {
	                                // Make the move
	                                piecePositions[newRow][newCol] = piecePositions[i][j];
	                                piecePositions[i][j] = 0;

	                                // Switch turns
	                                turn = (turn == 2) ? 1 : 2;

	                                // Refresh the board
	                                board.setIconProvider(this::icon);
	                                board.refresh();

	                                moveMade = true; // Indicate that a move was made
	                                break;
	                            }
	                        }
	                    }

	                    if (moveMade) break; // Break outer loop if move was made
	                }
	            }
	            if (moveMade) break; // Break outer loop if move was made
	        }

	        // If no valid moves found for any piece, end loop
	        if (!moveMade) {
	            board.showMessage("Sem movimentos válidos.");
	            return;
	        }
	    }
	}
	
	void save(String filename) {
	    try {
	        File file = new File(System.getProperty("user.dir"), filename + ".damas");
	        PrintWriter writer = new PrintWriter(file);
	        for (int[] row : piecePositions) {
	            for (int col : row) {
	                writer.print(col + " ");
	            }
	            writer.println();
	        }
	        writer.println(turn);
	        writer.close();
	        board.showMessage("Jogo guardado em: " + file.getAbsolutePath());
	    } catch (Exception e) {
	        board.showMessage("Erro ao guardar o jogo: " + e.getMessage());
	    }
	}
	void load(String filename) {
	    try {
	        File file = new File(System.getProperty("user.dir"), filename + ".damas");
	        System.out.println("A carregar: " + file.getAbsolutePath());

	        if (!file.exists()) {
	            board.showMessage("O ficheiro não foi encontrado em " + file.getAbsolutePath());
	            return;
	        }

	        Scanner scanner = new Scanner(file);

	        int size = piecePositions.length;
	        piecePositions = new int[size][size];

	        for (int i = 0; i < size; i++) {
	            String[] line = scanner.nextLine().trim().split(" ");
	            for (int j = 0; j < size; j++) {
	                piecePositions[i][j] = Integer.parseInt(line[j]);
	            }
	        }

	        if (scanner.hasNextLine()) {
	            turn = Integer.parseInt(scanner.nextLine().trim());
	        }

	        scanner.close();
	        board.setIconProvider(this::icon);
	        board.refresh();
	        board.showMessage("Jogo carregado!");
	    } catch (FileNotFoundException e) {
	        board.showMessage("O ficheiro não foi encontrado.");
	    } catch (Exception e) {
	        board.showMessage("Erro ao carregar o ficheiro. ");
	        e.printStackTrace();
	    }
	}
	
	Color background(int line, int col) { //checkerboard pattern
		if ((line+col)%2==0) {
			return StandardColor.WHITE;
		} else {
			return StandardColor.BLACK;
		}
	}
	
	Color backgroundSelection(int line, int col) { //checkerboard pattern
	   if ((line+col)%2==0) {
			return StandardColor.WHITE;
		} 
	   if (selectedPiece.line == new Position(line,col).line && selectedPiece.col == new Position(line,col).col) {
		        return StandardColor.YELLOW; // Set background to yellow if this is the selected piece
		    }
	   else {
				return StandardColor.BLACK;
			}
	}
	
	String icon(int line, int col) {
		if (piecePositions[line][col] == 1) {
            return "black.png"; 
        } else if (piecePositions[line][col] == 2) {
            return "white.png"; 
        }
        return null; 
	}
	
	

	void click(int line, int col) {
	    selectedPiece = new Position(line, col);
	    int piece = piecePositions[selectedPiece.getLine()][selectedPiece.getCol()];

	    // If the user clicked on a square with a piece
	    if (piece != 0) {
	        if (turn == piece) {
	            // Select the piece
	            lastselectedPiece = new Position(line, col);
	            board.setBackgroundProvider(this::backgroundSelection);  
	        } else {
	            board.showMessage("Não é a tua vez!");  // Show a message if it isn't your turn
	        }
	    } else {
	        board.setBackgroundProvider(this::background);  // Reset background for empty squares
	        
	        // If the user clicked on an empty square (destination)
	        piece = piecePositions[lastselectedPiece.getLine()][lastselectedPiece.getCol()];
	        if (isValidMove(selectedPiece, lastselectedPiece, piece)) {
	            // Move the piece to the new position
	            piecePositions[selectedPiece.getLine()][selectedPiece.getCol()] = piecePositions[lastselectedPiece.getLine()][lastselectedPiece.getCol()]; // give new position last piece value
	            piecePositions[lastselectedPiece.getLine()][lastselectedPiece.getCol()] = 0; // remove piece from last position

	            // After the move, reset the selected piece
	            selectedPiece = null;
	            board.setIconProvider(this::icon);  // Update the board icons
	            board.refresh();  // Refresh the board to reflect the move

	            // Update the turn
	            lastselectedPiece = new Position(line, col);
	            turn = (turn == 2) ? 1 : 2;  // Switch turns
	        } else {
	            board.showMessage("Movimento inválido!");  // Show a message if the move is invalid
	        }
	    }
	}
    

    // Check if a move is valid
	boolean isValidMove(Position endposition, Position startposition, int piece) {
	    // Ensure destination is empty for valid moves
	    if (piece == 2) { // White piece
	        // Regular move or jump logic for white
	        if (endposition.equals(new Position(startposition.getLine() - 1, startposition.getCol() - 1)) || endposition.equals(new Position(startposition.getLine() - 1, startposition.getCol() + 1))) {
	            return true;
	        }
	        if (endposition.equals(new Position(startposition.getLine() - 2, startposition.getCol() - 2)) || endposition.equals(new Position(startposition.getLine() - 2, startposition.getCol() + 2))) {
	            if (piecePositions[startposition.getLine() - 1][startposition.getCol() - 1] == 1) { // If enemy piece in between
	                piecePositions[startposition.getLine() - 1][startposition.getCol() - 1] = 0;
	                return true;
	            }
	            if (piecePositions[startposition.getLine() - 1][startposition.getCol() + 1] == 1) { // If enemy piece in between
	                piecePositions[startposition.getLine() - 1][startposition.getCol() + 1] = 0;
	                return true;
	            }
	        }
	        return false;
	    }

	    if (piece == 1) { // Black piece
	        // Regular move or jump logic for black
	        if (endposition.equals(new Position(startposition.getLine() + 1, startposition.getCol() - 1)) || endposition.equals(new Position(startposition.getLine() + 1, startposition.getCol() + 1))) {
	            return true;
	        }
	        if (endposition.equals(new Position(startposition.getLine() + 2, startposition.getCol() - 2)) || endposition.equals(new Position(startposition.getLine() + 2, startposition.getCol() + 2))) {
	            if (piecePositions[startposition.getLine() + 1][startposition.getCol() + 1] == 2) { // If enemy piece in between
	                piecePositions[startposition.getLine() + 1][startposition.getCol() + 1] = 0;
	                return true;
	            }
	            if (piecePositions[startposition.getLine() + 1][startposition.getCol() - 1] == 2) { // If enemy piece in between
	                piecePositions[startposition.getLine() + 1][startposition.getCol() - 1] = 0;
	                return true;
	            }
	        }
	        return false;
	    }

	    return false;
	}
	public static void main(String[] args) {
		View gui = new View(6);
		gui.start();
	}
}