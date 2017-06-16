#include <wiringPi.h>

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#define MAXTIMINGS	85
int dhtpin = 7;
int dht11_dat[5] = { 0, 0, 0, 0, 0 };

void read_dht11_dat()
{
	uint8_t laststate	= HIGH;
	uint8_t counter		= 0;
	uint8_t j		= 0, i;

	dht11_dat[0] = dht11_dat[1] = dht11_dat[2] = dht11_dat[3] = dht11_dat[4] = 0;

	/* pull pin down for 18 milliseconds */
	pinMode( dhtpin, OUTPUT );
	digitalWrite( dhtpin, LOW );
	delay( 18 );
	/* then pull it up for 40 microseconds */
	digitalWrite( dhtpin, HIGH );
	delayMicroseconds( 40 );
	/* prepare to read the pin */
	pinMode( dhtpin, INPUT );

	/* detect change and read data */
	for ( i = 0; i < MAXTIMINGS; i++ )
	{
		counter = 0;
		while ( digitalRead( dhtpin ) == laststate )
		{
			counter++;
			delayMicroseconds( 1 );
			if ( counter == 255 )
			{
				break;
			}
		}
		laststate = digitalRead( dhtpin );

		if ( counter == 255 )
			break;

		/* ignore first 3 transitions */
		if ( (i >= 4) && (i % 2 == 0) )
		{
			/* shove each bit into the storage bytes */
			dht11_dat[j / 8] <<= 1;
			if ( counter > 16 )
				dht11_dat[j / 8] |= 1;
			j++;
		}
	}

	/*
	 * check we read 40 bits (8bit x 5 ) + verify checksum in the last byte
	 * print it out if data is good
	 */
	if ( (j >= 40) &&
	     (dht11_dat[4] == ( (dht11_dat[0] + dht11_dat[1] + dht11_dat[2] + dht11_dat[3]) & 0xFF) ) )
	{
		printf( "%d.%d %d.%d\n",
			dht11_dat[2], dht11_dat[3], dht11_dat[0], dht11_dat[1]);
	}else  {
		printf( "ERROR\n" );
	}
}

int main(int argc, char * argv[])
{
	if ( wiringPiSetup() == -1 )
		exit( 1 );
	if(argc > 1)
    dhtpin = atoi(argv[1]);
    read_dht11_dat();
	return(0);
}