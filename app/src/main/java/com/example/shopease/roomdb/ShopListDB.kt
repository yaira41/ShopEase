import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.shopease.models.ShopListWithCoordinates

@Database(entities = [ShopListWithCoordinates::class], version = 1, exportSchema = false)
abstract class ShopListDB : RoomDatabase() {
    abstract fun shopListDao(): ShopListDao

    companion object {
        @Volatile
        private var INSTANCE: ShopListDB? = null

        fun getInstance(context: Context): ShopListDB {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ShopListDB::class.java,
                        "shop_list_database"
                    ).build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
