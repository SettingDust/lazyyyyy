package settingdust.lazyyyyy.forge.weaponmaster.faster_initilize

import com.sky.weaponmaster.reg.PieceReg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import settingdust.lazyyyyy.util.collect
import settingdust.lazyyyyy.util.concurrent
import settingdust.lazyyyyy.util.flatMap
import settingdust.lazyyyyy.util.reduce

object FasterParts {
    fun countEveryWeaponVariant() = runBlocking {
        withContext(Dispatchers.IO) {
            PieceReg.getWeapons().asFlow().concurrent()
                .flatMap { weaponID ->
                    val places = PieceReg.getPlaces(weaponID).toList()
                    val partsByPlace = places.associateWith { place ->
                        PieceReg.getParts(weaponID, place).toList()
                    }

                    val multipliers = calculateMultipliers(partsByPlace, places)
                    val totalVariants = multipliers.last() * (partsByPlace[places.last()]?.size ?: 1)

                    channelFlow {
                        (0 until totalVariants).asFlow().concurrent().collect { variant ->
                            val tickers = calculateTickers(variant, multipliers, partsByPlace, places)

                            val pieceCheck = places.mapIndexed { index, place ->
                                val part = partsByPlace[place]!![tickers[index]]
                                PieceReg.getPiece(weaponID, place, part)
                            }

                            if (PieceReg.checkValidWeapon(pieceCheck)) send(1L)
                        }
                    }
                }
                .reduce { acc, value -> acc + value }
        }
    }

    private fun calculateMultipliers(
        partsByPlace: Map<String, List<String>>,
        places: List<String>
    ): IntArray {
        val multipliers = IntArray(places.size)
        var product = 1
        for (i in places.indices.reversed()) {
            multipliers[i] = product
            product *= partsByPlace[places[i]]?.size ?: 1
        }
        return multipliers
    }

    private fun calculateTickers(
        variant: Int,
        multipliers: IntArray,
        partsByPlace: Map<String, List<String>>, // Adjust types based on actual data structure
        places: List<String>
    ): IntArray {
        val tickers = IntArray(places.size)
        var remainingVariant = variant

        for (index in places.indices) {
            val partsSize = partsByPlace[places[index]]!!.size
            tickers[index] = remainingVariant / multipliers[index] % partsSize
        }

        return tickers
    }
}