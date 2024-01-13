package com.example.shopease.friends

class ShowFriendsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FriendsAdapter
    private lateinit var username: String
    private val requestsDatabaseHelper = RequestsDatabaseHelper()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_show_friends, container, false)

        // Initialize RecyclerView and Adapter
        recyclerView = view.findViewById(R.id.recyclerViewFriends)
        adapter = FriendsAdapter(emptyList()) // Initial empty list

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        username = (activity as BaseActivity).username!!

        // Retrieve friends for the current user
        requestsDatabaseHelper.getFriends(username) { friends ->
            // Update the adapter with friends
            adapter.updateFriends(friends)
        }

        return view
    }
}
