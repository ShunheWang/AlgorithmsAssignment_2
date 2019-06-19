package player;

import java.util.ArrayList;
import java.util.Random;

import ship.Ship;
import world.World;
import world.World.ShipLocation;

/**
 * Random guess player (task A). Please implement this class.
 *
 * @author Shunhe Wang(s3587669),Natalie Sy(s3679718)
 */
public class RandomGuessPlayer implements Player {
	// Initialize map size
	private int mapRowSize;
	private int mapColumnSize;
	// Initialize a map for record which coordinate has been guessed
	private boolean[][] mapGuess;
	// Initialize player's ships
	private ArrayList<PlayerOwnShip> playerOwnShip;

	/*
	 * Initialize every ship
	 */
	private class PlayerOwnShip {
		// Record ship name and ship's width and length
		private Ship ship;
		// Record ship location's coordinates
		private ArrayList<String> shipLocation;
		// Record every coordinate has been hit
		private boolean[] isShoted;

		private PlayerOwnShip(int isBroken) {
			this.shipLocation = new ArrayList<String>();
			this.isShoted = new boolean[isBroken];
		}
	}

	@Override
	public void initialisePlayer(World world) {
		this.mapRowSize = world.numRow;
		this.mapColumnSize = world.numColumn;
		// Initialize mapGuess and every coordinate is false, which means all
		// coordinates have never been guessed
		initMapGuess(mapRowSize, mapColumnSize);
		// Initialize ships for every player
		initOwnerShip(world);
	}

	/*
	 * Initialize mapGuess
	 */
	private void initMapGuess(int mRow, int mCol) {
		this.mapGuess = new boolean[mRow][mCol];
		for (int i = 0; i < mapGuess.length; i++) {
			for (int j = 0; j < mapGuess[i].length; j++) {
				mapGuess[i][j] = false;
			}
		}
	}

	/*
	 * Initialize player's ships
	 */
	private void initOwnerShip(World world) {
		this.playerOwnShip = new ArrayList<PlayerOwnShip>();
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
				// The form of every coordinate is (x,y)
				String shipCoordinate = sRow + "," + sColumn;
				tem.shipLocation.add(shipCoordinate);
				// Every coordinate has not hit when initializing.
				tem.isShoted[j] = false;
			}
			// Add every ship into ArrayList for every player
			playerOwnShip.add(tem);
		}
	}

	@Override
	public Answer getAnswer(Guess guess) {
		Answer ans = new Answer();
		for (int i = 0; i < playerOwnShip.size(); i++) {
			PlayerOwnShip eachShip = this.playerOwnShip.get(i);
			for (int j = 0; j < eachShip.shipLocation.size(); j++) {
				// Get every coordinate for every ships, which is (x,y)
				String eachCoordinate = eachShip.shipLocation.get(j);
				// split this coordinate to get x value and y value separately
				String[] splitCoordinate = eachCoordinate.split(",");
				int shipRow = Integer.parseInt(splitCoordinate[0]);
				int shipCol = Integer.parseInt(splitCoordinate[1]);
				// if every the guess coordinate is equal to one of coordinate of every ship for
				// this player, which means ship has hit
				if (guess.row == shipRow && guess.column == shipCol) {
					eachShip.isShoted[j] = true;
					ans.isHit = true;
					// When all coordinates of one of ships is false, which means this ship is sunk,
					// answer.shipSunk is equals to this sunk ship
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

	@Override
	public Guess makeGuess() {
		Random random = new Random();
		Guess newGuess = new Guess();
		int rowGuess;
		int columnGuess;
		// Make a guess randomly and if this guess is true, which means this coordinate
		// has been hit, guess randomly again
		do {
			rowGuess = random.nextInt(mapRowSize);
			columnGuess = random.nextInt(mapColumnSize);
		} while (this.mapGuess[rowGuess][columnGuess] != false);
		newGuess.row = rowGuess;
		newGuess.column = columnGuess;
		return newGuess;
	}

	@Override
	public void update(Guess guess, Answer answer) {
		// Update the mapGuess
		this.mapGuess[guess.row][guess.column] = true;
		// Provide the guess coordinate
			System.out.println(guess.toString());
		// Provide the answer for guessing
			System.out.println(answer.toString());
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
