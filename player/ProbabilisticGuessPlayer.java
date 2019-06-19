package player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import ship.Ship;
import world.World;
import world.World.ShipLocation;

/**
 * Probabilistic guess player (task C). Please implement this class.
 *
 * @author Shunhe Wang(s3587669), Natalie Sy(s3679718)
 */
public class ProbabilisticGuessPlayer implements Player {
	// Initialize map row and column size
	private int mapRowSize;
	private int mapColumnSize;
	// Record the point is guessed or not for hunt model
	private boolean[][] mapGuess;
	// Record probability density in hunt model
	private int[][] hmProbDentity;
	// Store all ships for player
	private ArrayList<PlayerOwnShip> playerOwnShip;
	// Record remain ships that have not been guessed
	private ArrayList<PlayerOwnShip> remainGuessShips;
	// Initialize the control sign to handle makeGuess would enter into hunt model
	// or target model
	private String controlModel;
	// Store guessed and miss points in hunt model
	private ArrayList<Guess> shotedLocations;
	// Store hit points
	private ArrayList<Guess> shotedShipLocations;
	// Record sunk ships
	private ArrayList<Ship> recordSunkShips;
	// Initialize map to check which points has been guessed for target model
	private String[][] tmMap;
	// Calculate the probability density in target model
	private int[][] tmProbDentity;

	// Initialize ships for players
	private class PlayerOwnShip {
		private Ship ship;
		private ArrayList<String> shipLocation;
		private boolean[] isShoted;

		private PlayerOwnShip(int isBroken) {
			this.shipLocation = new ArrayList<String>();
			this.isShoted = new boolean[isBroken];
		}
	}

	@Override
	public void initialisePlayer(World world) {
		this.recordSunkShips = new ArrayList<Ship>();
		this.mapRowSize = world.numRow;
		this.mapColumnSize = world.numColumn;
		// Record all cell is guessed (hit) or not
		mapGuess = initMapGuess(mapRowSize, mapColumnSize);
		// Initialize all ships for each player
		this.playerOwnShip = new ArrayList<PlayerOwnShip>();
		initShips(world, this.playerOwnShip);
		// Initialize all guess ships
		this.remainGuessShips = new ArrayList<PlayerOwnShip>();
		initShips(world, this.remainGuessShips);
		// Initialize probability density for hunt model
		this.hmProbDentity = new int[mapRowSize][mapColumnSize];
		initHMProbabilityDentity(remainGuessShips);
		// Firstly, the control model is hunt model
		this.controlModel = "HM";
		shotedLocations = new ArrayList<Guess>();
	}

	/*
	 * Initialize the probability density for hunt model
	 */
	private void initHMProbabilityDentity(ArrayList<PlayerOwnShip> ships) {
		// cycle every ship
		for (PlayerOwnShip tem : ships) {
			// The first point is (0,0) in every ship
			int rowPoint = 0;
			int colPoint = 0;
			// Get ship width and length
			int sWidth = tem.ship.width();
			int sLen = tem.ship.len();

			// If the (row+this ship's width) is less than the map row size
			while ((rowPoint + sWidth - 1) < mapRowSize) {
				// If the (column+this ship's length) is less than the map column size
				while ((colPoint + sLen - 1) < mapColumnSize) {
					// Information goes inside,which means it can form a ship, so that probability
					// density of every related point adds 1
					for (int i = 0; i < sWidth; i++) {
						for (int j = 0; j < sLen; j++) {
							this.hmProbDentity[rowPoint + i][colPoint + j] += 1;
						}
					}
					//// Add column value adds 1 unit and row value unchanges.
					colPoint++;
				}
				// Add new row value goes up 1 unit and column value goes back zero
				colPoint = 0;
				rowPoint++;
			}
			// If ship's width is not equals, which means there is another sharp to form
			// this ship. This ship is like 1X3 or 2X3 instead of 2X2
			if (sWidth != sLen) {
				rowPoint = 0;
				colPoint = 0;
				while ((rowPoint + sLen - 1) < mapRowSize) {
					while ((colPoint + sWidth - 1) < mapColumnSize) {
						for (int i = 0; i < sLen; i++) {
							for (int j = 0; j < sWidth; j++) {
								this.hmProbDentity[rowPoint + i][colPoint + j]++;
							}
						}
						colPoint++;
					}
					colPoint = 0;
					rowPoint++;
				}
			}
		}
	}

	/*
	 * Initialize map to record guessed cells
	 */
	private boolean[][] initMapGuess(int mRow, int mCol) {
		boolean tem[][] = new boolean[mRow][mCol];
		for (int i = 0; i < tem.length; i++) {
			for (int j = 0; j < tem[i].length; j++) {
				tem[i][j] = false;
			}
		}
		return tem;
	}

	/*
	 * Initialize ships
	 */
	private void initShips(World world, ArrayList<PlayerOwnShip> ships) {
		ArrayList<ShipLocation> shipLocations = world.shipLocations;
		for (int i = 0; i < shipLocations.size(); i++) {
			ShipLocation sLocation = shipLocations.get(i);
			int lSize = sLocation.coordinates.size();
			PlayerOwnShip tem = new PlayerOwnShip(lSize);
			// Add ship name
			tem.ship = sLocation.ship;
			// Add all coordinates of this ship
			for (int j = 0; j < lSize; j++) {
				int sRow = sLocation.coordinates.get(j).row;
				int sColumn = sLocation.coordinates.get(j).column;
				String shipCoordinate = sRow + "," + sColumn;
				tem.shipLocation.add(shipCoordinate);
				tem.isShoted[j] = false;
			}
			ships.add(tem);
		}
	}

	@Override
	public Answer getAnswer(Guess guess) {
		Answer ans = new Answer();
		for (int i = 0; i < playerOwnShip.size(); i++) {
			PlayerOwnShip eachShip = this.playerOwnShip.get(i);
			for (int j = 0; j < eachShip.shipLocation.size(); j++) {
				String eachCoordinate = eachShip.shipLocation.get(j);
				String[] splitCoordinate = eachCoordinate.split(",");
				int shipRow = Integer.parseInt(splitCoordinate[0]);
				int shipCol = Integer.parseInt(splitCoordinate[1]);
				// If this guess point is one part of ship, which means player hits one part of
				// ship
				if (guess.row == shipRow && guess.column == shipCol) {
					eachShip.isShoted[j] = true;
					ans.isHit = true;
					// Check this ship is sunk or not
					for (int k = 0; k < eachShip.isShoted.length; k++) {
						if (eachShip.isShoted[k] == false) {
							return ans;
						}
					}
					ans.shipSunk = eachShip.ship;
					return ans;
				}
			}
		}
		return ans;
	}

	/*
	 * Return all largest possibility points currently
	 */
	private ArrayList<String> getLargestDentityPoint(int[][] probDentity) {
		ArrayList<String> tem = new ArrayList<String>();
		// Get largest point from density map
		int maxValue = probDentity[0][0];
		for (int i = 0; i < probDentity.length; i++) {
			for (int k = 0; k < probDentity[i].length; k++) {
				if (probDentity[i][k] > maxValue) { // max
					maxValue = probDentity[i][k];
				}
			}
		}
		// Use the max value above to collect all largest points to store into list
		for (int i = 0; i < probDentity.length; i++) {
			for (int k = 0; k < probDentity[i].length; k++) {
				if (probDentity[i][k] == maxValue) { // max
					String coordinate = i + "," + k;
					tem.add(coordinate);
				}
			}
		}
		return tem;
	}

	/*
	 * in hunt model, make a guess is based on hm probability density
	 */
	private Guess huntModel() {
		// Get all largest possibility points currently
		ArrayList<String> largestPossPoints = getLargestDentityPoint(this.hmProbDentity);
		Random random = new Random();
		Guess newGuess = new Guess();
		// Then select one point randomly from this list
		int index = random.nextInt(largestPossPoints.size());
		String[] array = largestPossPoints.get(index).split(",");
		newGuess.row = Integer.parseInt(array[0]);
		newGuess.column = Integer.parseInt(array[1]);

		return newGuess;
	}

	/*
	 * Update hunt model map and probability density
	 */
	private void updateHM(int gRow, int gCol) {
		for (PlayerOwnShip tem : remainGuessShips) {
			// Get guess row and column and every ship's width and length
			int rowPoint = gRow;
			int colPoint = gCol;
			int sWidth = tem.ship.width();
			int sLen = tem.ship.len();
			// To process update
			processHMUpd(sWidth, sLen, rowPoint, colPoint);
			// If ship's width is equals to length, like 2X2, we do not need to update.
			// Otherwise, swap ship's width and length to process again
			if (sWidth != sLen) {
				processHMUpd(sLen, sWidth, rowPoint, colPoint);
			}

		}
	}

	/*
	 * To process hunt model update
	 */
	private void processHMUpd(int sWidth, int sLen, int gRow, int gCol) {
		for (int i = 0; i < sWidth; i++) {
			// Get top and boundary for that guessed point
			int topBoundary = gRow + (sWidth - 1) - i;
			int downBoundary = gRow - i;
			if ((topBoundary) <= 9 && (downBoundary) >= 0) {
				for (int j = 0; j < sLen; j++) {
					// Get left and right boundary for that guessed point
					// It means which these points are in map range or not
					int leftBoundary = gCol - (sLen - 1) + j;
					int rightBoundary = gCol + j;
					if ((leftBoundary) >= 0 && (rightBoundary) <= 9) {
						// If check there is any point has been guessed, If it displays true, which
						// means it includes that point,
						// then no need to delete
						if (check(topBoundary, downBoundary, leftBoundary, rightBoundary) == false) {
							delete(topBoundary, downBoundary, leftBoundary, rightBoundary);
						}
					}
				}
			}
		}
	}

	/*
	 * Delete the possibility density
	 */
	private void delete(int top, int down, int left, int right) {
		int i = 0;
		while ((top + i) >= down) {
			int j = 0;
			while ((left + j) <= right) {
				this.hmProbDentity[top + i][left + j]--;
				j++;
			}
			i--;
		}
	}

	/*
	 * Check the points has been guessed exist in this range
	 */
	private boolean check(int top, int down, int left, int right) {
		int i = 0;
		while ((top + i) >= down) {
			int j = 0;
			while ((left + j) <= right) {
				if (mapGuess[top + i][left + j] == true) {
					return true;
				}
				j++;
			}
			i--;
		}
		return false;
	}

	@Override
	public Guess makeGuess() {
		Guess newGuess = null;
		// If control model = HM, makeGuess would enter into hunt model. Otherwise, it
		// would enter into target model
		if (controlModel.equals("HM")) {
			newGuess = huntModel();
		} else if (controlModel.equals("TM")) {
			newGuess = targetModel();
		}
		return newGuess;
	}

	/*
	 * Init hunt model map to record the guessed
	 */
	private String[][] initHMMap(int row, int col) {
		String tem[][] = new String[row][col];
		for (int i = 0; i < tem.length; i++) {
			for (int j = 0; j < tem[i].length; j++) {
				tem[i][j] = "F";
			}
		}
		return tem;
	}

	/*
	 * To update target model map and record the which point has been guessed
	 */
	private void addAllShotedLocation() {
		for (int i = 0; i < shotedLocations.size(); i++) {
			int row = shotedLocations.get(i).row;
			int col = shotedLocations.get(i).column;
			tmMap[row][col] = "T";
		}
	}

	/*
	 * makeGuess() enters into target model
	 */
	private Guess targetModel() {
		Guess newGuess = new Guess();
		ArrayList<String> largestPossPoints = getLargestDentityPoint(this.tmProbDentity);
		Random random = new Random();
		int index = random.nextInt(largestPossPoints.size());
		String[] array = largestPossPoints.get(index).split(",");
		newGuess.row = Integer.parseInt(array[0]);
		newGuess.column = Integer.parseInt(array[1]);
		return newGuess;
	}

	/*
	 * Initialize target model
	 */
	private void initTM() {
		// Initialize target density map & hit map
		if (this.tmMap == null && this.tmProbDentity == null) {
			this.tmMap = initHMMap(mapRowSize, mapColumnSize);
			this.tmProbDentity = new int[mapRowSize][mapColumnSize];
			shotedShipLocations = new ArrayList<Guess>();
			// Add the points that have been hit && empty location
			addAllShotedLocation();
		}
	}

	/*
	 * To update target model
	 */
	private void updateTM(int gRow, int gCol) {
		// Initialize the target model
		initTM();

		// update tm map
		for (PlayerOwnShip tem : remainGuessShips) {
			int rowPoint = gRow;
			int colPoint = gCol;
			int sWidth = tem.ship.width();
			int sLen = tem.ship.len();
			// To update target model
			processTMUpd(sWidth, sLen, rowPoint, colPoint);
			if (sWidth != sLen) {
				processTMUpd(sLen, sWidth, rowPoint, colPoint);
			}
		}
	}

	/*
	 * To process target model update
	 */
	private void processTMUpd(int sWidth, int sLen, int gRow, int gCol) {
		for (int i = 0; i < sWidth; i++) {
			// Get top and down boundary
			int topBoundary = gRow + (sWidth - 1) - i;
			int downBoundary = gRow - i;
			if ((topBoundary) <= 9 && (downBoundary) >= 0) {
				for (int j = 0; j < sLen; j++) {
					// Get left and right boundary
					int leftBoundary = gCol - (sLen - 1) + j;
					int rightBoundary = gCol + j;
					if ((leftBoundary) >= 0 && (rightBoundary) <= 9) {
						// To check there are some points has been guessed
						if (checkTM(topBoundary, downBoundary, leftBoundary, rightBoundary) == false) {
							// We need to consider if some points are hit points. No need to add possibility
							// density for these points
							// In other words,only tmMap[x][y] is false, we add possibility density
							addTMProbDentity(topBoundary, downBoundary, leftBoundary, rightBoundary);
						}
					}
				}
			}
		}
	}

	/*
	 * To add possibility density in target model
	 */
	private void addTMProbDentity(int top, int down, int left, int right) {
		int i = 0;
		while ((top + i) >= down) {
			int j = 0;
			while ((left + j) <= right) {
				if (this.tmMap[top + i][left + j].equals("F")) {
					this.tmProbDentity[top + i][left + j]++;
				}
				j++;
			}
			i--;
		}
	}

	/*
	 * Update empty location in target model
	 */
	private void updateEmptyLocationInTM(int gRow, int gCol) {
		for (PlayerOwnShip tem : remainGuessShips) {
			int rowPoint = gRow;
			int colPoint = gCol;
			int sWidth = tem.ship.width();
			int sLen = tem.ship.len();
			processTMEmptyLocation(sWidth, sLen, rowPoint, colPoint);
			if (sWidth != sLen) {
				processTMEmptyLocation(sLen, sWidth, rowPoint, colPoint);
			}

		}
	}

	/*
	 * Check there are some points has been guessed before in target model
	 */
	private boolean checkTM(int top, int down, int left, int right) {
		int i = 0;
		while ((top + i) >= down) {
			int j = 0;
			while ((left + j) <= right) {
				if (this.tmMap[top + i][left + j].equals("T")) {
					return true;
				}
				j++;
			}
			i--;
		}
		return false;
	}

	/*
	 * Return ship coordinates counts
	 */
	private int hasShipLocation(int top, int down, int left, int right) {
		int count = 0;
		int i = 0;
		while ((top + i) >= down) {
			int j = 0;
			while ((left + j) <= right) {
				if (this.tmMap[top + i][left + j].equals("-")) {
					count++;
				}
				j++;
			}
			i--;
		}
		return count;
	}

	/*
	 * Delete density in target model.In detail, delete the possibility density of
	 * the points has possibility density and this point is not hit point, delete
	 * the count of the possibility density of empty points for each points are
	 * impacted by empty points
	 */
	private void delete(int top, int down, int left, int right, int count) {
		int i = 0;
		while ((top + i) >= down) {
			int j = 0;
			while ((left + j) <= right) {
				if (!this.tmMap[top + i][left + j].equals("-")) {
					if (this.tmProbDentity[top + i][left + j] > 0) {
						this.tmProbDentity[top + i][left + j] -= count;
					}
				}
				j++;
			}
			i--;
		}
	}

	/*
	 * To process empty point in target model
	 */
	private void processTMEmptyLocation(int sWidth, int sLen, int gRow, int gCol) {
		for (int i = 0; i < sWidth; i++) {
			int topBoundary = gRow + (sWidth - 1) - i;
			int downBoundary = gRow - i;
			if ((topBoundary) <= 9 && (downBoundary) >= 0) {
				for (int j = 0; j < sLen; j++) {
					int leftBoundary = gCol - (sLen - 1) + j;
					int rightBoundary = gCol + j;
					if ((leftBoundary) >= 0 && (rightBoundary) <= 9) {
						// If return true, which means it has deleted already
						if (checkTM(topBoundary, downBoundary, leftBoundary, rightBoundary) == false) {
							// Get how many possibility counts to delete for impacted points
							int deleteCount = hasShipLocation(topBoundary, downBoundary, leftBoundary, rightBoundary);
							// If count > 0, we would delete
							if (deleteCount > 0) {
								delete(topBoundary, downBoundary, leftBoundary, rightBoundary, deleteCount);
							}

						}
					}
				}
			}
		}
	}

	@Override
	public void update(Guess guess, Answer answer) {
		// If hit point is miss
		if (answer.isHit == false) {
			// Update hunt model for density
			updateHM(guess.row, guess.column);
			// Update hunt model map
			mapGuess[guess.row][guess.column] = true;
			// Change possibility density of this point is 0
			hmProbDentity[guess.row][guess.column] = 0;
			// Store this point into list for initializing the possibility density of target
			// model
			shotedLocations.add(guess);
			// When control model is target model
			if (controlModel.equals("TM")) {
				// Update the possibility density of target model if the possibility density of
				// this point is not 0, which means it impact other points
				if (tmProbDentity[guess.row][guess.column] != 0) {
					// Update the possibility density of target model.
					updateEmptyLocationInTM(guess.row, guess.column);
				}
				// Update target model map, which means that this map would record this point
				// has been hit
				tmMap[guess.row][guess.column] = "T";
			}
			// print.out();
		} else {
			// If guess point is hit
			if (answer.shipSunk == null) {
				controlModel = "TM";
				// Update the possibility density in target model
				updateTM(guess.row, guess.column);
				// Record this points in target model map
				this.tmMap[guess.row][guess.column] = "-";
				// Change the possibility density of that point in target model
				this.tmProbDentity[guess.row][guess.column] = 0;
				// Add this guessed point into list for update the possibility density of hunt
				// model after control model changes back to hunt model
				shotedShipLocations.add(guess);
				// print.out();
			} else {
				// If guess point is hit and one ship is sunk
				// Add this point into list
				shotedShipLocations.add(guess);
				// Get this sunk ship
				Ship sunkShip = answer.shipSunk;
				// Add this sunk into list because maybe the total count of hit points is over
				// than this coordinate counts of this sunk ship
				// Which means 1).If two count equals, all coordinate of this sunk ship only
				// stored in the list,thus we will handle something then
				// control model would change hunt model. @).If two count not equals, some part
				// of other ship is hit and stored into list, except
				// this sunk ship, thus control model would be still target model
				this.recordSunkShips.add(sunkShip);
				// Get the count of all hit point in list
				int sunkShipCells = 0;
				for (Ship ships : recordSunkShips) {
					sunkShipCells += ships.width() * sunkShip.len();
				}

				// If this count stored in list is equals to the coordinate count of this sunk
				// ship
				if (sunkShipCells == shotedShipLocations.size()) {
					// Update all related information in hunt model and transfer these points into
					// another list
					for (int i = 0; i < shotedShipLocations.size(); i++) {
						Guess g = shotedShipLocations.get(i);
						int row = g.row;
						;
						int col = g.column;
						updateHM(row, col);
						mapGuess[row][col] = true;
						hmProbDentity[row][col] = 0;
						Guess shotedLocation = new Guess();
						shotedLocation.row = row;
						shotedLocation.column = col;
						shotedLocations.add(shotedLocation);
					}
					// To process delete all possibility density of this sunk ship in hunt model
					// because this ship has been sunk
					for (Ship ships : recordSunkShips) {
						deleteSunkShipDentity(ships);
					}
					// In remainGuessShips, we would delete this ship because this ship has been
					// sunk.For next guessing and caculating
					// possibility density, we would not consider this ship
					for (Ship sunkShips : recordSunkShips) {
						for (Iterator<PlayerOwnShip> it = remainGuessShips.iterator(); it.hasNext();) {
							PlayerOwnShip gus = it.next();
							if (sunkShips.name().equals(gus.ship.name())) {
								it.remove();
							}
						}
					}
					// Remove all information in target model and back to hunt model from target
					// model
					shotedShipLocations = null;
					this.tmMap = null;
					this.tmProbDentity = null;
					recordSunkShips.clear();
					controlModel = "HM";
				} else if (sunkShipCells > shotedShipLocations.size()) {
					// List stored some points of parts of other ships
					// We only update all information about target model
					// For next guessing, we also use target model
					updateTM(guess.row, guess.column);
					this.tmMap[guess.row][guess.column] = "-";
					this.tmProbDentity[guess.row][guess.column] = 0;
					controlModel = "TM";
				}
			}
		}
		// Provide the guess coordinate
			System.out.println(guess.toString());
		// Provide the answer for guessing
			System.out.println(answer.toString());
	}

	/*
	 * Delete possibility density for sunk ship
	 */
	private void deleteSunkShipDentity(Ship sunkShip) {
		int ssWidth = 0;
		int ssLen = 0;
		for (PlayerOwnShip s : remainGuessShips) {
			if (sunkShip.name().equals(s.ship.name())) {
				// Get ship's width and length
				ssWidth = s.ship.width();
				ssLen = s.ship.len();
				break;
			}
		}
		// To process delete possibility density for this sunk ship
		processDeleteSunkShipDentity(ssWidth, ssLen);
		// If width is not equals to length, swap ship's width and length, then process
		// again
		if (ssWidth != ssLen) {
			processDeleteSunkShipDentity(ssLen, ssWidth);
		}

	}

	/*
	 * To handle delete sunk ship density
	 */
	private void processDeleteSunkShipDentity(int width, int len) {
		int rowPoint = 0;
		int colPoint = 0;
		int sWidth = width;
		int sLen = len;
		int top = rowPoint + sWidth - 1;
		while (top < mapRowSize) {
			int right = colPoint + sLen - 1;
			while (right < mapColumnSize) {
				if (!check(top, rowPoint, colPoint, right)) {
					for (int i = 0; i < sWidth; i++) {
						for (int j = 0; j < sLen; j++) {
							this.hmProbDentity[rowPoint + i][colPoint + j]--;
						}
					}
				}
				right++;
				colPoint++;
			}
			colPoint = 0;
			rowPoint++;
			top++;
		}
	}

	@Override
	public boolean noRemainingShips() {
		for (PlayerOwnShip ship : playerOwnShip) {
			for (boolean shot : ship.isShoted) {
				if (shot == false) {
					return false;
				}
			}
		}
		return true;
	}

}
