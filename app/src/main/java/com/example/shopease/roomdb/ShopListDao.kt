import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.shopease.models.ShopListWithCoordinates

@Dao
interface ShopListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertShopList(shopList: ShopListWithCoordinates)
}