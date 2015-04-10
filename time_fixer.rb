
# TODO change so that it can also take stdin and fix the values.
file_name = nil
if ARGV.size == 1
    file_name = ARGV[0]
else 
    Kernel.exit(true)
end

file = File.new("#{file_name}.fixed", "w+")

IO.foreach(file_name) do |line|

    # Scan the line and check if it is the real value
    result = line.scan(/real[\s\t]+([0-9]+)m([0-9]+\.[0-9]+)s/)
    if result && result[0]
        # total it up
        time = result[0][0].to_i * 60 + result[0][1].to_f
        file.puts time
    end
end