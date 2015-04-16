
# Parse the time string
def handleLine(io, line)
    # Scan the line and check if it is the real value
    result = line.scan(/real[\s\t]+([0-9]+)m([0-9]+\.[0-9]+)s/)
    if result && result[0]
        # total it up
        time = result[0][0].to_i * 60 + result[0][1].to_f
        io.puts time
    end
end

file_name = nil
from_stdin = false
if ARGV.size == 1
    file_name = ARGV[0]
else
    from_stdin = true
#    Kernel.exit(true)
end


# Handle the type of input
if !from_stdin
    file = File.new("#{file_name}.fixed", "w+")
    IO.foreach(file_name) do |line|
        handleLine(file, line)
    end
    file.close()
else
    # Read in all of the lines.
    while !$stdin.eof?
        line = gets
        #puts "FIXER"
        handleLine($stdout, line)
    end
end