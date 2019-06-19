package player;

import java.util.ArrayList;
import java.util.Random;
import ship.Ship;
import world.World;
import world.World.ShipLocation;

/**
 * Greedy guess player (task B). Please implement this class.
 *
 * @author Shunhe Wang(s3587669), Natalie Sy(s3679718)
 */
public class GreedyGuessPlayer implements Player {
	private int record;// Use to hunt model uses firstly or not
	private ArrayList<Guess> allPossibleGuess;
	// Record last hunt point before hunt model changes to target model
	private Guess lastHuntingGuess;
	// Record previous point for next guess point
	private Guess previousGuess;
	// Init map size
	private int mapRowSize;
	private int mapColumnSize;
	private boolean[][] isShoted;
	// Init all ships for players
	private ArrayList<PlayerOwnShip> playerOwnShip;

	/*
	 * Record every ship for player
	 */
	private class PlayerOwnShip {
		private Ship ship;
		private ArrayList<String> allLocation;
		private boolean[] isBroken;

		private PlayerOwnShip(int isBroken) {
			this.isBroken = new boolean[isBroken];
			this.allLocation = new ArrayList<String>();
		}
	}

	@Override
	public void initialisePlayer(World world) {
		this.lastHuntingGuess = new Guess();
		this.allPossibleGuess = new ArrayList<Guess>();
		this.previousGuess = new Guess();
		this.record = 0;
		this.mapRowSize = world.numRow;
		this.mapColumnSize = world.numColumn;
		// Record all cell is guessed (shotted) or not
		this.isShoted = new boolean[mapRowSize][mapColumnSize];
		for (int i = 0; i < isShoted.length; i++) {
			for (int j = 0; j < isShoted[i].length; j++) {
				isShoted[i][j] = false;
			}
		}
		// init all ships for each player
		this.playerOwnShip = new ArrayList<PlayerOwnShip>();
		for (int i = 0; i < world.shipLocations.size(); i++) {
			ShipLocation sl = world.shipLocations.get(i);
			PlayerOwnShip tem = new PlayerOwnShip(sl.coordinates.size());
			tem.ship = sl.ship;
			// init all shipLocation
			for (int j = 0; j < sl.coordinates.size(); j++) {
				int rowCell = sl.coordinates.get(j).row;
				int columnCell = sl.coordinates.get(j).column;
				String shipCoordinate = rowCell + "," + columnCell;
				tem.allLocation.add(shipCoordinate);
				tem.isBroken[j] = false;
			}
			playerOwnShip.add(tem);
		}
	}

	@Override
	public Answer getAnswer(Guess guess) {
		Answer ans = new Answer();
		for (int i = 0; i < playerOwnShip.size(); i++) {
			PlayerOwnShip eachShip = this.playerOwnShip.get(i);
			for (int j = 0; j < eachShip.allLocation.size(); j++) {
				String eachCoordinate = eachShip.allLocation.get(j);
				String[] splitCoordinate = eachCoordinate.split(",");
				int shipRow = Integer.parseInt(splitCoordinate[0]);
				int shipCol = Integer.parseInt(splitCoordinate[1]);
				// handle one ship's coordinate is hit
				if (guess.row == shipRow && guess.column == shipCol) {
					eachShip.isBroken[j] = true;
					ans.isHit = true;
					// Check this ship is sunk or not
					for (int k = 0; k < eachShip.isBroken.length; k++) {
						if (eachShip.isBroken[k] == false) {
							return ans;
						}
					}
					// The info goes out from for loop above, which means this ship has been sunk
					ans.shipSunk = eachShip.ship;
					return ans;
				}
			}
		}
		return ans;
	}

	@Override
	public Guess makeGuess() {
		Guess newGuess = null;
		// If possibleGuess list is empty, which means player guesses miss point and
		// enter hunt model
		if (this.allPossibleGuess.isEmpty()) {
			newGuess = this.huntingMode();
			// Record last hunt point because if this guess point is hit ship, then next
			// model is target model and then makeGuess is based on
			// target model. When target model returns to hunt model,we use this recorded
			// point to start to find to avoid miss possible ship location.
			this.lastHuntingGuess.row = newGuess.row;
			this.lastHuntingGuess.column = newGuess.column;
		} else {
			newGuess = this.targetMode();
		}
		// Record previous points
		this.previousGuess.row = newGuess.row;
		this.previousGuess.column = newGuess.column;
		return newGuess;
	}

	/*
	 * When hit one part of any ship, enter target model
	 */
	private Guess targetMode() {
		// Return first possible point for queue relate to previous hit point and then
		// remove this point because this point has been guessed
		Guess guess = this.allPossibleGuess.get(0);
		this.allPossibleGuess.remove(0);
		return guess;
	}

	/*
	 * Check point is guessed or not
	 */
	private boolean isGuessed(int gRow, int gCol) {
		if (isShoted[gRow][gCol] == false) {
			return false;
		}
		return true;
	}

	/*
	 * Hunt Model is to find possible ship point
	 */
	private Guess huntingMode() {
		Guess newGuess = new Guess();
		// for first time, hunt model will use guess randomly
		if (record == 0) {
			newGuess = this.firstGuess();
		} else {
			// Use last hunt point to continue find possible ship point
			// If next guess point is in same colume
			if ((this.lastHuntingGuess.column + 2) <= 9) {
				// Check this point has not been guessed
				if (!this.isGuessed(this.lastHuntingGuess.row, this.lastHuntingGuess.column + 2)) {
					newGuess.column = this.lastHuntingGuess.column + 2;
					newGuess.row = this.lastHuntingGuess.row;
				} else {
					// If guessed,colume+=2 and then go into hunting model again
					this.lastHuntingGuess.column = this.lastHuntingGuess.column + 2;
					newGuess = this.huntingMode();
				}
				// If next guess point is in different colume
			} else {
				// If last point column is 8, which means next column is 1
				if (this.lastHuntingGuess.column == 8) {
					this.lastHuntingGuess.column = 1;
					// (lastHuntingGuess.row-1)<0,which means next point row is 9
					if ((this.lastHuntingGuess.row - 1) < 0) {
						this.lastHuntingGuess.row = 9;
						// Check this point has not been guessed
						if (!this.isGuessed(this.lastHuntingGuess.row, this.lastHuntingGuess.column)) {
							newGuess.column = this.lastHuntingGuess.column;
							newGuess.row = this.lastHuntingGuess.row;
						} else {
							// If guessed,colume+=2 and then go into hunting model again
							this.lastHuntingGuess.column = this.lastHuntingGuess.column + 2;
							newGuess = this.huntingMode();
						}
					} else {
						this.lastHuntingGuess.row = this.lastHuntingGuess.row - 1;
						// Check this point has not been guessed
						if (!this.isGuessed(this.lastHuntingGuess.row, this.lastHuntingGuess.column)) {
							newGuess.column = this.lastHuntingGuess.column;
							newGuess.row = this.lastHuntingGuess.row;
						} else {
							// If guessed,colume+=2 and then go into hunting model again
							this.lastHuntingGuess.column = this.lastHuntingGuess.column + 2;
							newGuess = this.huntingMode();
						}
					}
				} else if (this.lastHuntingGuess.column == 9) {
					// If last point column is 9, which means next column is 0
					this.lastHuntingGuess.column = 0;
					// newGuess.column=0;
					if ((this.lastHuntingGuess.row - 1) < 0) {
						this.lastHuntingGuess.row = 9;
						// Check this point has not been guessed
						if (!this.isGuessed(this.lastHuntingGuess.row, this.lastHuntingGuess.column)) {
							newGuess.column = this.lastHuntingGuess.column;
							newGuess.row = this.lastHuntingGuess.row;
						} else {
							this.lastHuntingGuess.column = this.lastHuntingGuess.column + 2;
							newGuess = this.huntingMode();
						}
						// newGuess.row=9;
					} else {
						this.lastHuntingGuess.row = this.lastHuntingGuess.row - 1;
						// Check this point has not been guessed
						if (!this.isGuessed(this.lastHuntingGuess.row, this.lastHuntingGuess.column)) {
							newGuess.column = this.lastHuntingGuess.column;
							newGuess.row = this.lastHuntingGuess.row;
						} else {
							this.lastHuntingGuess.column = this.lastHuntingGuess.column + 2;
							newGuess = this.huntingMode();
						}
					}
				}
			}
		}

		return newGuess;
	}

	/*
	 * Handle hunt model at first time by using guess randomly
	 */
	private Guess firstGuess() {
		Random random = new Random();
		Guess newGuess = new Guess();
		int rowGuess = random.nextInt(mapRowSize);
		int columnGuess = random.nextInt(mapColumnSize);
		this.isShoted[rowGuess][columnGuess] = true;
		newGuess.row = rowGuess;
		newGuess.column = columnGuess;
		this.record++;
		return newGuess;
	}

	@Override
	public void update(Guess guess, Answer answer) {
		this.isShoted[guess.row][guess.column] = true;
		if (answer.isHit == true) {
			this.addPossibleGuess();
		}
		// Provide the guess coordinate
			System.out.println(guess.toString());
		// Provide the answer for guessing
			System.out.println(answer.toString());
	}

	/*
	 * Check the point that will be added into list,has been added before
	 */
	private Guess getPossibleGuess(int gRow, int gCol) {
		for (Guess g : allPossibleGuess) {
			if (gRow == g.row && gCol == g.column) {
				return null;
			}
		}
		Guess guess = new Guess();
		guess.row = gRow;
		guess.column = gCol;
		return guess;
	}

	/*
	 * When one part of ship is hit, add 4 point arount this point into list
	 */
	private void addPossibleGuess() {
		// If this point existed in map and has never been guessed
		if ((this.previousGuess.row + 1) <= 9
				&& this.isShoted[this.previousGuess.row + 1][this.previousGuess.column] == false) {
			// Check this point has added already and if not add, add this point into list
			Guess possGuess = getPossibleGuess(this.previousGuess.row + 1, this.previousGuess.column);
			if (possGuess != null) {
				allPossibleGuess.add(possGuess);
			}

		}
		// If this point existed in map and has never been guessed
		if ((this.previousGuess.column + 1) <= 9
				&& this.isShoted[this.previousGuess.row][this.previousGuess.column + 1] == false) {
			// Check this point has added already and if not add, add this point into list
			Guess possGuess = getPossibleGuess(this.previousGuess.row, this.previousGuess.column + 1);
			if (possGuess != null) {
				allPossibleGuess.add(possGuess);
			}
		}
		// If this point existed in map and has never been guessed
		if ((this.previousGuess.row - 1) >= 0
				&& this.isShoted[this.previousGuess.row - 1][this.previousGuess.column] == false) {
			// Check this point has added already and if not add, add this point into list
			Guess possGuess = getPossibleGuess(this.previousGuess.row - 1, this.previousGuess.column);
			if (possGuess != null) {
				allPossibleGuess.add(possGuess);
			}
		}
		// If this point existed in map and has never been guessed
		if ((this.previousGuess.column - 1) >= 0
				&& this.isShoted[this.previousGuess.row][this.previousGuess.column - 1] == false) {
			// Check this point has added already and if not add, add this point into list
			Guess possGuess = getPossibleGuess(this.previousGuess.row, this.previousGuess.column - 1);
			if (possGuess != null) {
				allPossibleGuess.add(possGuess);
			}
		}
	}

	@Override
	public boolean noRemainingShips() {
		for (int i = 0; i < playerOwnShip.size(); i++) {
			PlayerOwnShip eachShip = this.playerOwnShip.get(i);
			for (int j = 0; j < eachShip.isBroken.length; j++) {
				if (eachShip.isBroken[j] == false) {
					return false;
				}
			}
		}
		return true;
	}

}
