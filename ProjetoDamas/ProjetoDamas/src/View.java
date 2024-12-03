import pt.iscte.guitoo.Color;
import pt.iscte.guitoo.StandardColor;
import pt.iscte.guitoo.board.Board;

public class View {
	Board board;
	int[][] piecePositions;
	
	View(int size) {
		board = new Board("...", size, size,40); //tem de ser flexivel de forma a que o jogar possa impor dimensoes
		piecePositions = new int[size][size];
        initializePieces(size);
		board.addAction("aleatorio",this::random);
		board.addAction("novo", this::action); //adiciona botao
		board.addAction("gravar",this::save);
		board.addAction("carregar",this::load);
		board.setBackgroundProvider(this::background); //sets background
		board.setIconProvider(this::icon);
		board.addMouseListener(this::handleclick);
		
	}

	void start() {
		board.open();
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

	}
	
	void save() {
		
	}
	
	void load() {
		
	}
	
	Color background(int line, int col) { //checkerboard pattern
		if ((line+col)%2==0) {
			return StandardColor.WHITE;
		} else {
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
	
	private int[] selectedPiece = null;

    void handleclick(int line, int col) {
        if (selectedPiece == null) {
            // Select a piece
            if (piecePositions[line][col] != 0) {
                selectedPiece = new int[] { line, col };
                board.showMessage("Peça selecionada em: (" + line + ", " + col + ")");
            }
        } else {
            // Try to move the selected piece
            if (isValidMove(selectedPiece[0], selectedPiece[1], line, col)) {
                piecePositions[line][col] = piecePositions[selectedPiece[0]][selectedPiece[1]];
                piecePositions[selectedPiece[0]][selectedPiece[1]] = 0;
                selectedPiece = null;
                board.refresh();
            } else {
                board.showMessage("Movimento inválido!");
                selectedPiece = null;
            }
        }
    }

    // Check if a move is valid
    boolean isValidMove(int fromLine, int fromCol, int toLine, int toCol) {
        // Ensure destination is empty
        if (piecePositions[toLine][toCol] != 0) return false;

        int piece = piecePositions[fromLine][fromCol];
        int dLine = toLine - fromLine;
        int dCol = Math.abs(toCol - fromCol);

        // Check diagonal movement
        if (dCol != 1 || Math.abs(dLine) != 1) return false;

        // Black pieces move up
        if (piece == 1 && dLine == -1) return true;

        // White pieces move down
        if (piece == 2 && dLine == 1) return true;

        return false;
    }

	public static void main(String[] args) {
		View gui = new View(6);
		gui.start();
	}
}