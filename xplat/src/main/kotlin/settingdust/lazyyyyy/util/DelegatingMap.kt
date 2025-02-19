package settingdust.lazyyyyy.util

class DelegatingMap<K, V>(val wrapped: MutableMap<K, V>) : MutableMap<K, V> by wrapped