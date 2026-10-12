package cz.vsb.fei.bluetoothdevicelocator.data

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import kotlin.concurrent.Volatile

/**
 * Bluetooth devices locator database.
 */
@Database(entities = [DeviceEntity::class, LocationEntity::class, DeviceLocationCrossRef::class], version = 1)
abstract class LocatorDatabase : RoomDatabase() {

    /**
     * Gets the data access object for DeviceEntity.
     */
    abstract fun deviceDao(): DeviceDao

    /**
     * Gets the data access object for LocationEntity.
     */
    abstract fun locationDao(): LocationDao

    /**
     * Gets the data access object for DeviceLocationCrossRef.
     */
    abstract fun deviceLocationCrossRefDao(): DeviceLocationCrossRefDao

    /**
     * Gets the data access object containing transaction related to Device and Location entities.
     */
    abstract fun deviceLocationTransactionsDao(): DeviceLocationTransactionsDao

    companion object {
        @Volatile
        private var INSTANCE: LocatorDatabase? = null

        /**
         * Gets the database instance.
         * @param context Application context.
         * @return Database instance.
         */
        fun getDatabase(context: Context): LocatorDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder<LocatorDatabase>(
                    context.applicationContext,
                    name = "bluetooth_device_locator_db"
                ).build()
                INSTANCE = db
                return db
            }
        }
    }

}