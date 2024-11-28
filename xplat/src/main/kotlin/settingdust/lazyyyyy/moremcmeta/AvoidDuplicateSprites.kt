package settingdust.lazyyyyy.moremcmeta

import java.util.*
import java.util.concurrent.LinkedBlockingQueue

class SetBackingQueue<T>(val backing: LinkedHashSet<T> = linkedSetOf<T>()) : Queue<T>, MutableSet<T> by backing {
    override fun offer(e: T) = backing.add(e)

    override fun remove() = backing.first().also {
        backing.remove(it)
    }

    override fun poll() = backing.firstOrNull()?.also {
        backing.remove(it)
    }

    override fun element() = backing.first()

    override fun peek() = backing.firstOrNull()
}

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class SetBackingLinkedBlockingQueue<T>(val backing: SetBackingQueue<T> = SetBackingQueue<T>()) :
    LinkedBlockingQueue<T>(), Queue<T> by backing